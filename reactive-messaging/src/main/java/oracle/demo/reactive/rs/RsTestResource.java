package oracle.demo.reactive.rs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
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

import oracle.demo.common.Processor;

@ApplicationScoped
@Path("/reactive")
public class RsTestResource {

    private static final Logger logger = Logger.getLogger(RsTestResource.class.getSimpleName());
    private final String defaultStr = "abc,lmn,xyz";

    @Inject private Processor processor;

    
    /**
     * Reactive Streams インターフェースをスクラッチで実装
     * curl localhost:8080/reactive/rs?str=abc,lmn,xyz
     */
    @GET @Path("/rs") @Produces(MediaType.TEXT_PLAIN)
    public String callRs(@QueryParam("str") String str) {

        final RsPublisher<RsMessage> publisher = new RsPublisher<>("RsPublisher");
        final RsSubscriber<RsMessage> subscriber = new RsSubscriber<>(
            "RsSubscriber", 
            4, // buffer size
            message -> message.complete(processor.process(message.getRequest())) // onNext()の実際の処理
          );
        publisher.subscribe(subscriber);

        try{
            return measure(() -> {
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map((x) -> {
                    logger.info("map >> " + x);
                    final RsMessage message = new RsMessage(x);
                    publisher.submit(message); // submitはブロックされない
                    return message.getResponse(); // CompletableFuture<String>
                })
                .collect(Collectors.toList()) // List<CompletableFuture>
                .stream()
                .map(CompletableFuture::join) // 実行結果を取得 - ここでブロックされる
                .collect(Collectors.joining(","));
            });
        }finally{
            publisher.close();
        }
    }

    /**
     * Reactive Streams インターフェースをスクラッチで実装 - 並列処理
     * curl localhost:8080/reactive/rs-multi?str=abc,lmn,xyz
     */
    @GET @Path("/rs-multi") @Produces(MediaType.TEXT_PLAIN)
    public String callRsMulti(@QueryParam("str") String str) {

        final List<RsPublisher<RsMessage>> publishers = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger();

        try{
            return measure(() -> {
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map((x) -> {
                    logger.info("map >> " + x);
                    final RsPublisher<RsMessage> publisher = new RsPublisher<>("RsPublisher-" + counter.incrementAndGet());
                    publishers.add(publisher);
                    final RsSubscriber<RsMessage> subscriber = new RsSubscriber<>(
                        "RsSubscriber-" + counter.get(), 
                        message -> message.complete(processor.process(message.getRequest())) // onNext()の実際の処理
                      );
                    publisher.subscribe(subscriber);
                    final RsMessage message = new RsMessage(x);
                    publisher.submit(message); // submitはブロックされない
                    return message.getResponse(); // CompletableFuture<String>
                })
                .collect(Collectors.toList()) // List<CompletableFuture>
                .stream()
                .map(CompletableFuture::join) // 実行結果を取得 - ここでブロックされる
                .collect(Collectors.joining(","));
            });
        }finally{
            publishers.forEach((p) -> p.close());
        }
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


