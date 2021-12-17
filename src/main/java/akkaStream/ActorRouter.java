package akkaStream;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ActorRouter {

    private static final String URL_QUERY = "url";
    private static final String REQUEST_NUMBER_QUERY = "url";

    private final static int TIMEOUT = 5000;


    public Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorSystem system, ActorMaterializer materializer) {
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));

        return Flow.of(HttpRequest.class)
                .map(request -> {
                    Query query = request.getUri().query();
                    String url  = query.get(URL_QUERY).toString();
                    Integer requestNumber = Integer.parseInt(query.get(REQUEST_NUMBER_QUERY).toString());
                    return new Pair<>(url, requestNumber);
                })
                .mapAsync(10, (param) -> {
                   return Patterns.ask(storeActor, param.first(), TIMEOUT)
                            .thenCompose(response -> {

                                if ((int) response.second() != 0) {
                                    return CompletableFuture.completedFuture(new Pair<>(param, response));
                                }

                                Flow<Pair<String, Integer>, Integer, NotUsed> flow = createFlow();

                                Sink testSink = Sink.fold(0, Integer::sum);
                                return Source.from(Collections.singletonList(param))
                                        .toMat(testSink, Keep.right())
                                        .run(materializer);
//                                        .thenApply(sum -> new Pair<>(pair.getKey(), sum / pair.getValue()));

                            });
                }).map(param -> {
                    return HttpResponse.create().withEntity(HttpEntities.create(param.toString()));
                }
        );
    }

    private Flow<Pair<String, Integer>, Integer, NotUsed> createFlow(){
       return Flow.<Pair<String, Integer>>create()
                .mapConcat(pair ->
                        new ArrayList<>(Collections.nCopies(pair.second(), pair.first()))
                )
                .mapAsync(pair -> {
                            long startTime = System.currentTimeMillis();
                            asyncHttpClient().prepareGet(pair.first()).execute();
                            long endTime = System.currentTimeMillis();

                            return CompletableFuture.completedFuture(new Pair<>(param, endTime - startTime));
                        }
                );
    }
}
