package oracle.demo.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Asynchronous;

@ApplicationScoped
@Path("/")
public class Processor {

    private static Logger logger = Logger.getLogger(Processor.class.getSimpleName());

    @Inject @ConfigProperty(name="demo.processor.delay", defaultValue="3000")
    private long delay;


    /**
     * シンプルバージョン 
     */
    public String process(String s) {
        final String result = new StringBuilder(s).reverse().toString().toUpperCase();
        logger.info("process() >> " + s + " -> " + result);

        // 意図的に3秒間スリープ
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {}

        return result;
    }

    
    /**
     * 非同期バージョン
     * MicroProfile Faault Tolerance アノテーション付 
     */
    @Asynchronous // MicroProfile Faault Tolerance
    public CompletionStage<String> processAsync(String s) {
        return CompletableFuture.completedFuture(process(s));
    }

    /**
     * RESTサービスバージョン
     * /process
     */
    @GET @Path("/process") @Produces(MediaType.TEXT_PLAIN)
    public String processRestService(@QueryParam("str") String str) {
        return process(str);
    }


}