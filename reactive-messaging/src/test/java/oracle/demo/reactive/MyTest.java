package oracle.demo.reactive;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Logger;

import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.AddConfig;
import io.helidon.microprofile.tests.junit5.DisableDiscovery;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import oracle.demo.messaging.basic.MessagingTestResource;
import oracle.demo.messaging.basic.PublisherBean;
import oracle.demo.messaging.basic.SubscriberBean;
import oracle.demo.messaging.processor.MsgProcessingBean;
import oracle.demo.messaging.processor.ProcessorTestResource;

import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;

@HelidonTest
@AddConfig(key = "demo.processor.delay", value = "100")
public class MyTest{
    
    private final Logger logger = Logger.getLogger(MyTest.class.getName());

    @Inject private WebTarget webTarget;

    @Test
    public void test(){
        logger.info(webTarget.getUri().toString());
        String response = webTarget.path("/reactive-messaging/basic").queryParam("str", "abc,lmn,xyz") .request().get(String.class);
        assertTrue(response.matches("Result: CBA,NML,ZYX - Elapsed time\\(ms\\): \\d+\\n"));
    }

}
