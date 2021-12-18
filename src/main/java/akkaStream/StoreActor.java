package akkaStream;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class StoreActor extends AbstractActor {
    private Map<String, Integer> storage = new HashMap<>();

    @Override
    public Receive createReceive(){
        return receiveBuilder(
        ).match(
                TestInformation.class, result -> {
                    sender().tell(getResult(result), ActorRef.noSender());
                }
        ).match(
                TestResult.class, this::setTestResult
        ).build();
    }

    private void setTestResult(TestResult testResult) {
        Integer time = Math.toIntExact(testResult.getTime());
        storage.putIfAbsent(testResult.getUrl(), time);
    };

    private TestResult getResult(TestInformation information) {
        long requestTime = 0;
        if (storage.containsKey(storage.containsKey(information.getUrl()))) {
            requestTime = storage.get(information.getUrl());
        }
        TestResult result = new TestResult(information.getUrl(), information.getRequestNumber(), requestTime);
        return result;
    }
}
