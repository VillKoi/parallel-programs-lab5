package akkaStream;

import akka.actor.AbstractActor;

import java.util.HashMap;
import java.util.Map;

public class ActorSystem extends AbstractActor {
    private Map<String, Map<String, TestInformation>> storage = new HashMap<>();

    @Override
    public Receive createReceive(){
        return receiveBuilder(
        ).match(
                TestInformation.class, this::setTestResult
        ).match(
                String.class, packageID -> {
                    sender().tell(getResult(packageID), self());
                }
        ).build();
    }
}
