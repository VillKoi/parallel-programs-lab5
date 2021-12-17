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
                TestResult.class, result -> {
                    sender().tell(getResult(result), self());
                }
        ).build();
    }

    private void setTestResult(TestInformation testResult) {
        storage.putIfAbsent(testResult.getUrl(), testResult.getRequestNumber());
    };

    private  Pair<String, Integer> getResult(TestResult result) {
        long  requestTime = storage.get(result.getUrl());
        Pair<String, Integer> output = new Pair<>(result.getUrl(), requestTime);
        return output;
    }
}
