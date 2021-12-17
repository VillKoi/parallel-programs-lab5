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
                TestInformation.class, result -> {
                    sender().tell(getResult(result), self());
                }

        ).match(
                TestResult.class, this::setTestResult
        ).build();
    }

    private void setTestResult(TestResult testResult) {
        storage.putIfAbsent(testResult.getUrl(), testResult());
    };

    private  Pair<String, Integer> getResult(TestInformation information) {
        long  requestTime = storage.get(information.getUrl());
        Pair<String, Integer> output = new Pair<>(information.getUrl(), requestTime);
        return output;
    }
}
