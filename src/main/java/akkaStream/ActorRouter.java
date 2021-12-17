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
import java.util.concurrent.CompletionStage;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ActorRouter {
    private ActorRef storeActor;

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

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
                .mapAsync(10, param -> {
                    TestInformation information = new TestInformation(param.first(), param.second());
                   return Patterns.ask(storeActor, information, TIMEOUT)
                            .thenCompose(response -> {

                                if ((int) response.second() != 0) {
                                    return CompletableFuture.completedFuture(new Pair<>(param, response));
                                }

                                Sink testSink = createFlow();
                                return Source.from(Collections.singletonList(param))
                                        .toMat(testSink, Keep.right())
                                        .run(materializer);
                            });
                }).map(param -> {
                    HttpResponse.create().withEntity(HttpEntities.create(param.toString()));
                });
    }

    private Sink<Pair<String, Integer>, CompletionStage<Long>> createFlow(){
       return Flow.<Pair<String, Integer>>create()
                .mapConcat(pair ->
                        new ArrayList<>(Collections.nCopies(pair.second(), pair.first()))
                )
                .mapAsync(param -> {
                            long startTime = System.currentTimeMillis();
                            asyncHttpClient().prepareGet(pair.first()).execute();
                            long endTime = System.currentTimeMillis();

                            return CompletableFuture.completedFuture(new Pair<>(param, endTime - startTime));
                        }
                ).fold(new TestInformation("", 0),(res, element) -> res.add((TestInformation) element)
                ).map(param -> {
                   storeActor.tell(param, ActorRef.noSender());
                   return param;
               }).toMat(Sink.fold(), Keep.right());
    }
}
