
package demo.messaging;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import demo.messaging.KeyValueMessage.KeyValue;

@Path("/")
@ApplicationScoped
public class MessagingResource {
    private final static Logger logger = Logger.getLogger(MsgProcessingBean.class.getName());

    @Inject MsgProcessingBean processor;

    @GET @Path("/submit")
    public void submit(@QueryParam("key") String key, @QueryParam("value") String value) {
        logger.info(String.format("@GET /submit: key=%s, value=%s", key, value));
        processor.submit(KeyValueMessage.of(new KeyValue(key, value)));
    }

    @POST @Path("/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    public void submit(KeyValue kv) {
        logger.info(String.format("@POST /submit: key=%s, value=%s", kv.getKey(), kv.getValue()));
        processor.submit(KeyValueMessage.of(kv));
    }

    @GET @Path("/bulk-submit")
    public void submit() {
        for(int i = 0 ; i < (256 + 10) ; i++){
            String key = "key";
            String value = Integer.toString(i+1);
            logger.info(String.format("@GET /submit: key=%s, value=%s", key, value));
            processor.submit(KeyValueMessage.of(new KeyValue(key, value)));
        }
    }

    @GET @Path("/close")
    public void close() {
        processor.close();
    }


}
