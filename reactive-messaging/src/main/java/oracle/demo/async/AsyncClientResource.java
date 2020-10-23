package oracle.demo.async;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import oracle.demo.common.Processor;


@ApplicationScoped
@Path("/async-client")
public class AsyncClientResource {

    private static Logger logger = Logger.getLogger(AsyncClientResource.class.getName());
    private final Client client = ClientBuilder.newClient(); // thread safe
    private final String defaultStr = "abc,lmn,xyz";

    @Inject private Processor processor;

    ////////////////////////////////////////////

    /**
     * JAX-RS 同期RESTクライアント
     * curl localhost:8080/async-client/sync?str=abc,lmn,xyz
     */
    @GET @Path("/sync") @Produces(MediaType.TEXT_PLAIN)
    public String callSync(@QueryParam("str") String str, @Context UriInfo uriInfo) {

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .map(x -> {
                return client
                .target(uriInfo.getBaseUri()).path("/process")
                .queryParam("str", x)
                .request()
                .get(String.class); // String
            })
            .collect(Collectors.joining(","));
        });

    }

    /**
     * JAX-RS 非同期RESTクライアント
     * curl localhost:8080/async-client/async?str=abc,lmn,xyz
     */
    @GET @Path("/async") @Produces(MediaType.TEXT_PLAIN)
    public String callAsync(@QueryParam("str") String str, @Context UriInfo uriInfo) {

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .map(x -> {
                return client
                .target(uriInfo.getBaseUri()).path("/process")
                .queryParam("str", x)
                .request()
                .async() // !!!
                .get(String.class);  // Future<String>
            })
            .collect(Collectors.toList()) // List<Future>
            .stream()
            .map(f -> { // 実行結果を取得: Future<String> -> String
                try{
                    return f.get();
                }catch(Exception e){ throw new RuntimeException(e.getMessage(), e); }
            }) 
            .collect(Collectors.joining(","));
        });

    }

    /**
     * JAX-RS Reactive RESTクライアント
     * curl localhost:8080/async-client/rx?str=abc,lmn,xyz
     */
    @GET @Path("/rx") @Produces(MediaType.TEXT_PLAIN)
    public String callRx(@QueryParam("str") String str, @Context UriInfo uriInfo) {

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .map(x -> {
                return client
                .target(uriInfo.getBaseUri()).path("/process")
                .queryParam("str", x)
                .request()
                .rx() // !!!
                .get(String.class); // CompletionStage<String>
            })
            .collect(Collectors.toList()) // List<CompletionStage>
            .stream()
            .map(f -> f.toCompletableFuture().join()) // CompletionStage<String> -> String
            .collect(Collectors.joining(","));
        });

    }


    /**
     * MicroProfile 非同期RESTクライアント - 呼び出し用インターフェース
     * ※タイプセーフな呼び出しがミソ / 返り値を CompletionStage にすると非同期になる
     */
    @Path("/")
    public static interface ProcessClient {
        @GET @Path("/process") @Produces(MediaType.TEXT_PLAIN)
        public CompletionStage<String> process(@QueryParam("str") String str);
    }

    /**
     * MicroProfile 非同期RESTクライアント
     * curl localhost:8080/async-client/mp?str=abc,lmn,xyz
     */
    @GET @Path("/mp") @Produces(MediaType.TEXT_PLAIN)
    public String callMP(@QueryParam("str") String str, @Context UriInfo uriInfo) {

        final ProcessClient client = RestClientBuilder.newBuilder()
            .baseUri(uriInfo.getBaseUri())
            .build(ProcessClient.class); // Interface !!

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .map(x -> {
                return client.process(x); // CompletionStage<String>
            })
            .collect(Collectors.toList()) // List<CompletionStage>
            .stream()
            .map(f -> f.toCompletableFuture().join()) // CompletionStage<String> -> String 
            .collect(Collectors.joining(","));
        });

    }



    /**
     * 実行時間を計測してレスポンスを返す
     */
    private String measure(Supplier<String> supplier){
        long start = System.currentTimeMillis();
        String result = supplier.get();
        long end = System.currentTimeMillis();
        String response = String.format("Result: %s - Elapsed time(ms): %d\n", result, end - start);
        logger.info(response);
        return response;
    }

}

