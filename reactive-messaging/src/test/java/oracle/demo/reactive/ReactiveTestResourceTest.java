package oracle.demo.reactive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.ws.rs.client.WebTarget;

import oracle.demo.TestServer;

public class ReactiveTestResourceTest extends TestServer{
    
    //@Test
    public void test(){

        webTarget.path("/openapi").request().get();


        Assertions.assertTrue(true);
    }



}
