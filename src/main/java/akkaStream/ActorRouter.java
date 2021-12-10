package akkaStream;

import akka.NotUsed;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Queue;

import static akka.http.javadsl.server.Directives.*;

public class ActorRouter {

    private static final String URL_QUERY = "url";
    private static final String REQUEST_NUMBER_QUERY = "url";

    public Route createRouter() {
        return route(
                get(() -> concat(
                        path(RESULT_PATH_, () -> parameter(RESULT_QUERY, key -> {
                            Future<Object> res = Patterns.ask(storeActor, key, TIMEOUT);
                            return completeOKWithFuture(res, Jackson.marshaller());
                        }))
                )),
                post(() -> concat(
                        path(TEST_RUN_PATH, ()->
                                entity(
                                        Jackson.unmarshaller(TestInputData.class), body ->  {
                                            ArrayList<TestInformation> tests = body.GetTests();
                                            for (TestInformation t: tests) {
                                                testActor.tell(t, storeActor);
                                            }
                                            return complete(StatusCodes.OK);
                                        })))
                ));
    }

    public Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorMaterializer materializer) {
        Flow.of(HttpRequest.class).map(
                request -> {
                    Query query = request.getUri().query();
                    String url  = query.get(URL_QUERY).toString();
                    int requestNumber = Integer.parseInt(query.get(REQUEST_NUMBER_QUERY).toString());
                    

                }
        )
    }
}
