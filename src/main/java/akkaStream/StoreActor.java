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
                // результат тестирования
                TestInformation.class, this::setTestResult
        ).match(
                // поиск результата
                String.class, url -> {
                    sender().tell(getResult(url), self());
                }
        ).build();
    }

    private void setTestResult(TestInformation testResult) {
        if (!storage.containsKey(testResult.getPackageID())) {
            storage.put(testResult.getPackageID(), new HashMap<>());
        }

        storage.get(testResult.getPackageID()).put(testResult.getTestName(), testResult);
    };

    private  Pair<String, Integer> getResult(String url) {
        Integer  requestNumber = storage.get(url);
        Pair<String, Integer>  result = new Pair<>(url, requestNumber);

        return result;
    }
}
