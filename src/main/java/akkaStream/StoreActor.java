package akkaStream;

import akka.actor.AbstractActor;

import java.util.HashMap;
import java.util.Map;

public class StoreActor extends AbstractActor {
    private Map<String, TestInformation> storage = new HashMap<>();

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

    private Map<String, String> getResult(String packageID) {
        Map<String, TestInformation>  testResults = storage.get(packageID);
        Map<String, String>  result = new HashMap<>();
        for (TestInformation r: testResults.values()) {
            System.out.println(r.toString());
            result.put(r.getTestName(), r.toString());
        }
        return result;
    }
}
