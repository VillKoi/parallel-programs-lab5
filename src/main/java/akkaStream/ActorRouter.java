package akkaStream;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javafx.util.Pair;

import static akka.http.javadsl.server.Directives.*;

public class ActorRouter {

    private static final String URL_QUERY = "url";
    private static final String REQUEST_NUMBER_QUERY = "url";


    public Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorSystem system, ActorMaterializer materializer) {
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));

        Flow.of(HttpRequest.class)
                .map(
                request -> {
                    Query query = request.getUri().query();
                    String url  = query.get(URL_QUERY).toString();
                    Integer requestNumber = Integer.parseInt(query.get(REQUEST_NUMBER_QUERY).toString());
                    Pair<String, Integer> startInformation = new Pair<>(url, requestNumber);
                    return startInformation;
                }).mapAsync(10, param -> {
                    Patterns.ask(storeActor, param).thenCompose(
                            res -> {
                                if (res != 0) {
                                    return CompletableFuture.completedFuture(new Pair<>(param, res));
                                }

                                Flow.<Pair<String, Integer>>.create()
                                        .mapConcat(pair ->
                                                new ArrayList<>(Collections.nCopies(pair))
                                        )
                                        .mapAsync(pair -> {
                                            currentTime = System.currentTimeMillis();
                                                    asyncHttpClient()

                                                    future
                                                }

                                        )
                                return Source.from(Collections.singletonList(r))
                                        .toMat(Sink.fold(), Keep.right()).run(materializer);


                            });
        }).map(

        );
    }
}
