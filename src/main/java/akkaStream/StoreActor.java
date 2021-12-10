package akkaStream;

import akka.actor.AbstractActor;
import jdk.internal.net.http.common.Pair;

import java.util.HashMap;
import java.util.Map;

public class StoreActor extends AbstractActor {
    private Map<String, Integer> storage = new HashMap<>();

    @Override
    public Receive createReceive(){
        return receiveBuilder(
        ).match(
                TestInformation.class, this::setTestResult

        ).match(
                String.class, url -> {
                    sender().tell(getResult(url), self());
                }
        ).build();
    }

    private void setTestResult(TestInformation testResult) {
        storage.putIfAbsent(testResult.getUrl(), testResult.getRequestNumber());
    };

    private  Pair<String, Integer> getResult(String url) {
        Integer  requestNumber = storage.get(url);
        Pair<String, Integer> result = new Pair<>(url, requestNumber);

        return result;
    }
}
