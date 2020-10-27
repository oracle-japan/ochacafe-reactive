package oracle.demo.messaging.basic;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import oracle.demo.reactive.FlowMessage;

@ApplicationScoped
@Path("/reactive-messaging")
public class MessagingTestResource {

    private static final Logger logger = Logger.getLogger(MessagingTestResource.class.getName());
    private final String defaultStr = "abc,lmn,xyz";

    @Inject
    private PublisherBean publisher;

    /**
     * 非同期呼び出しパターン - MicroProfile Reactive Streams Messagning で実装
     * curl localhost:8080/reactive-messaging/basic?str=abc,lmn,xyz
     */
    @GET @Path("/basic") @Produces(MediaType.TEXT_PLAIN)
    public String callWithReactiveMessaging(@QueryParam("str") String str) {

        return measure(() -> {
            try{
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map(x -> {
                    final FlowMessage message = new FlowMessage(x);
                    publisher.submit(message);
                    return message.getResponse(); // CompletableFuture
                })
                .collect(Collectors.toList()) // List<CompletableFuture>
                .stream()
                .map(CompletableFuture::join) // 実行結果を取得
                .reduce(null, (x, y) -> Optional.ofNullable(x).isPresent() ? x + "," + y : y);
            }catch(Exception e){
                throw new RuntimeException("Reactive Streams Messaging error: " + e.getMessage());
            }
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


