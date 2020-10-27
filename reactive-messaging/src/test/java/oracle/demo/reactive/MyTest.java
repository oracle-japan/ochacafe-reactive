package oracle.demo.reactive;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import io.helidon.microprofile.tests.junit5.AddConfig;
import io.helidon.microprofile.tests.junit5.DisableDiscovery;
import io.helidon.microprofile.tests.junit5.HelidonTest;

import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;

@HelidonTest
@DisableDiscovery
//@AddConfig(key = "app.greeting", value = "TestHello")
public class MyTest{
    
    //@Inject WebTarget webTarget;

    @Test
    public void test(){
        //webTarget.path("/metrics").request().get();
        assertTrue(true);
    }

}
