
package oracle.demo.messaging.connector;

import java.util.Arrays;
import java.util.Objects;
//import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
