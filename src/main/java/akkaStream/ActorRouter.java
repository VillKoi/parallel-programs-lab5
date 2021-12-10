package akkaStream;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import scala.concurrent.Future;

import java.util.ArrayList;

import static akka.http.javadsl.server.Directives.*;

public class ActorRouter {


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
}
