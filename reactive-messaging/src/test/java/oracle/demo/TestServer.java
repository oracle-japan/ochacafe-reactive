package oracle.demo;

import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.helidon.microprofile.server.Server;

public class TestServer {

    protected static Server server;
    protected static Client client;
    protected static WebTarget webTarget;

    //@BeforeAll
    public static void beforeAll(){
        System.out.println("Starting server...");
        //server = Server.builder().build();
        server = Server.create();
        //server.start();
        System.out.println("Server port: " + server.port());

        client = ClientBuilder.newClient();
        
        webTarget = client.target("http://localhost:" + server.port());
    }

    //@AfterAll
    public static void afterAll(){
        System.out.println("Stopping server...");
        Optional.ofNullable(client).ifPresent(c -> c.close());
        Optional.ofNullable(server).ifPresent(s -> s.stop());
    }

    //@Test
    public void test(){

        webTarget.path("/openapi").request().get();


        Assertions.assertTrue(true);
    }


}
