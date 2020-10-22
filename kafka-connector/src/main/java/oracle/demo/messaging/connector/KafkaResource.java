
package oracle.demo.messaging.connector;

import java.util.Objects;
//import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/kafka")
@ApplicationScoped
public class KafkaResource {
    //private final static Logger logger = Logger.getLogger(KafkaResource.class.getName());

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

}
