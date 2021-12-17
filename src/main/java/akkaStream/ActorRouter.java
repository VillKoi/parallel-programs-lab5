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

import java.util.Optional;
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

    public Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorMaterializer materializer) {
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
                                Optional<TestResult> result = (Optional<TestResult>) response;
                                if (result.isReady()) {
                                    return CompletableFuture.completedFuture(new Pair<>(param, result));
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
                        new ArrayList<>(Collections.nCopies(pair.second(), pair))
                )
                .mapAsync(10, param -> {
                            long startTime = System.currentTimeMillis();
                            asyncHttpClient().prepareGet(param.first()).execute();
                            long endTime = System.currentTimeMillis();

                            return CompletableFuture.completedFuture(new TestResult(param.first(), 0,endTime - startTime));
                        }
                ).fold(new TestResult("", 0, 0),(res, element) ->
                       res.add(element)
                ).map(param -> {
                   storeActor.tell(param, ActorRef.noSender());
                   return param;
               }).toMat(Sink.head(), Keep.right());
    }
}
