package akkaStream;

import akka.actor.AbstractActor;

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
        Integer time = Math.toIntExact(testResult.getTime());
        storage.putIfAbsent(testResult.getUrl(), time);
    };

    private TestResult getResult(TestInformation information) {
        long  requestTime = storage.getOrDefault(information.getUrl(), 0);
        TestResult result = new TestResult(information.getUrl(), information.getRequestNumber(), requestTime);
        return result;
    }
}
