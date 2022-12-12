
package demo.messaging;

import java.util.concurrent.TimeUnit;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.helidon.microprofile.server.Server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MainTest {

    private static Server server;

    @BeforeAll
    public static void startTheServer() {
        server = Server.create().start();
    }

    @Test
    void testSubmit() {
        Client client = ClientBuilder.newClient();

        Response r = client
              .target(getConnectionString("/submit?key=key1&value=val1"))
              .request()
              .get();
        Assertions.assertEquals(204, r.getStatus(), "GET status code"); // no content

        r = client
                .target(getConnectionString("/submit"))
                .request()
                .post(Entity.entity("{\"key\" : \"key2\", \"value\" : \"val2\"}", MediaType.APPLICATION_JSON));
        Assertions.assertEquals(204, r.getStatus(), "POST status code");

        try{
            TimeUnit.MILLISECONDS.sleep(2000);
        }catch(InterruptedException e){}
        r = client
              .target(getConnectionString("/bulk-submit"))
              .request()
              .get();


        try{
            TimeUnit.MILLISECONDS.sleep(15 * 1000);
        }catch(InterruptedException e){}
        r = client
              .target(getConnectionString("/close"))
              .request()
              .get();

    }

    @AfterAll
    static void destroyClass() {
        CDI<Object> current = CDI.current();
        ((SeContainer) current).close();
    }

    private String getConnectionString(String path) {
        return "http://localhost:" + server.port() + path;
    }
}
