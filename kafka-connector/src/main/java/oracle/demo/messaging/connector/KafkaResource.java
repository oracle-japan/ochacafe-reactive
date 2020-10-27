
package oracle.demo.messaging.connector;

import java.util.Arrays;
import java.util.Objects;
//import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/kafka")
@ApplicationScoped
public class KafkaResource {
    //private final static Logger logger = Logger.getLogger(KafkaResource.class.getSimpleName());

    @Inject
    private KafkaPublisher publisher;

    /**
     * curl localhost:8080/kafka/publish?message=Hello+Ochacafe%21
     */
    @GET @Path("/publish") @Produces(MediaType.TEXT_PLAIN)
    public String submit(@QueryParam("message") String message) {
        Objects.requireNonNull(message);
        publisher.submit(message);
        return "OK\n";
    }

    /**
     * curl -X POST -H "Content-type: application/json" -d '["Ochacafe #1","Ochacafe #2","Ochacafe #3","Ochacafe #4","Ochacafe #5","Ochacafe #6","Ochacafe #7","Ochacafe #8"]' localhost:8080/kafka/publish
     */
    @POST @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.TEXT_PLAIN)
    public String submitPost(String[] messages) {
        Objects.requireNonNull(messages);
        Arrays.stream(messages).forEach(publisher::submit);
        return "OK\n";
    }


}
