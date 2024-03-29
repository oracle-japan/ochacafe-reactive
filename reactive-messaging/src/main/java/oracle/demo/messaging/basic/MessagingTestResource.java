package oracle.demo.messaging.basic;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import oracle.demo.reactive.FlowMessage;

@ApplicationScoped
@Path("/reactive-messaging")
public class MessagingTestResource {

    private static final Logger logger = Logger.getLogger(MessagingTestResource.class.getSimpleName());
    private final String defaultStr = "abc,lmn,xyz";

    @Inject
    private PublisherBean publisher;

    /**
     * 非同期呼び出しパターン - MicroProfile Reactive Messagning で実装
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
                throw new RuntimeException("Reactive Messaging error: " + e.getMessage());
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


