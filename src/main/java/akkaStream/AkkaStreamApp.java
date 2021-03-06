package akkaStream;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.javadsl.Flow;

public class AkkaStreamApp {
    private final static String HOST = "localhost";
    private final static int PORT = 8080;

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");

        ActorRouter router = new ActorRouter();
        
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        router.setStoreActor(system.actorOf(Props.create(StoreActor.class)));
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = router.createFlow(materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST, PORT),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }
}
