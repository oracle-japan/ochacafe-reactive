
package oracle.demo.messaging.processor;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import oracle.demo.messaging.processor.KeyValueMessage.KeyValue;

@Path("/reactive-messaging")
@ApplicationScoped
public class ProcessorTestResource {
    private final static Logger logger = Logger.getLogger(ProcessorTestResource.class.getSimpleName());

    @Inject
    private MsgProcessingBean processor;

    /**
     * MicroProfile Reactive Messagning - Processor でチャネルを連結
     * curl localhost:8080/reactive-messaging/process/key1?value=val1
     */
    @GET @Path("/process/{key}") @Produces(MediaType.TEXT_PLAIN)
    public String submit(@PathParam("key") String key, @QueryParam("value") String value) {
        logger.info(String.format("@GET /submit: key=%s, value=%s", key, value));
        KeyValue kv = new KeyValue(key, value);
        processor.submit(kv);
        return kv.getResponse();
    }

    /**
     * MicroProfile Reactive Messagning - Processor でチャネルを連結
     * curl -X POST -H "Content-type: application/json" -d '{"key":"key1","value":"val1"}' localhost:8080/reactive-messaging/process
     */
    @POST @Path("/process") @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String submit(KeyValue kv) {
        logger.info(String.format("@POST /submit: key=%s, value=%s", kv.getKey(), kv.getValue()));
        processor.submit(kv);
        return kv.getResponse();
    }

}
