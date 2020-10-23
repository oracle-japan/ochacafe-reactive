package oracle.demo.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import io.helidon.common.configurable.ThreadPoolSupplier;
import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import oracle.demo.common.Processor;

@ApplicationScoped
@Path("/reactive")
public class ReactiveTestResource {

    private static final Logger logger = Logger.getLogger(ReactiveTestResource.class.getName());
    private final String defaultStr = "abc,lmn,xyz";

    @Inject private Processor processor;

    private final ExecutorService es = ThreadPoolSupplier.builder().threadNamePrefix("reactive-").build().get();



    /**
     * Flow を使って、pub/sub方式でメッセージを渡す
     * 送信メッセージのオブジェクトから処理結果を非同期に取得できるようにしている
     * curl localhost:8080/reactive/flow?str=abc,lmn,xyz
     */
    @GET @Path("/flow") @Produces(MediaType.TEXT_PLAIN)
    public String callFlow(@QueryParam("str") String str) {

        final SubmissionPublisher<FlowMessage> publisher = new SubmissionPublisher<>(es, Flow.defaultBufferSize());
        publisher.subscribe(new FlowSubscriber());

        try{
            return measure(() -> {
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map(x -> {
                    FlowMessage message = new FlowMessage(x);
                    publisher.submit(message); // submitはブロックされない
                    logger.info(String.format("EML=%d, EMD=%d", publisher.estimateMaximumLag(), publisher.estimateMinimumDemand()));
                    return message.getResponse(); // 実行結果はsubscriberがCompletableFutureを更新する
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
     * Flow を使って、pub/sub方式でメッセージを渡す - Publisherを要素分作成して並列処理を行う
     * curl localhost:8080/reactive/flow-multi?str=abc,lmn,xyz
     */
    @GET @Path("/flow-multi") @Produces(MediaType.TEXT_PLAIN)
    public String callFlowMulti(@QueryParam("str") String str) {

        final List<SubmissionPublisher<FlowMessage>> publishers = new ArrayList<>();

        try{
            return measure(() -> {
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map(x -> {
                    SubmissionPublisher<FlowMessage> publisher = 
                        new SubmissionPublisher<>(es, Flow.defaultBufferSize()); // Publisherを要素分作成
                    publishers.add(publisher);
                    publisher.subscribe(new FlowSubscriber()); // Subscriberも要素分作成
                    FlowMessage message = new FlowMessage(x);
                    publisher.submit(message); // submitはブロックされない
                    logger.info(String.format("EML=%d, EMD=%d", publisher.estimateMaximumLag(), publisher.estimateMinimumDemand()));
                    return message.getResponse(); // 実行結果はsubscriberがCompletableFutureを更新する
                })
                .collect(Collectors.toList()) // List<CompletableFuture>
                .stream()
                .map(CompletableFuture::join) // 実行結果を取得 - ここでブロックされる
                .collect(Collectors.joining(","));
            });
        }finally{
            publishers.forEach(publisher -> publisher.close());
        }
    }


    
    /**
     * MicroProfile Reactive Streams Operators を使う
     * curl localhost:8080/reactive/operators?str=abc,lmn,xyz
     */
    @GET @Path("/operators") @Produces(MediaType.TEXT_PLAIN)
    public String callReactive(@QueryParam("str") String str) {

        return measure(() -> {
            return ReactiveStreams
            .of(Optional.ofNullable(str).orElse(defaultStr).split(",")) // PublisherBuilder
            .map(processor::process) // PublisherBuilder
            .collect(Collectors.joining(",")) // CompletionRunner
            .run() // CompletionStage
            .toCompletableFuture() // CompletableFuture
            .join(); // String
        });
    }



    /**
     * RxJava を使う - flatMapを使って並列処理にする
     * curl localhost:8080/reactive/rxjava?str=abc,lmn,xyz
     */
    @GET @Path("/rxjava") @Produces(MediaType.TEXT_PLAIN)
    public String callRxJava(@QueryParam("str") String str) throws Exception {

        final String[] args = Optional.ofNullable(str).orElse(defaultStr).split(",");
        final ConcurrentHashMap<Object, String> dict = new ConcurrentHashMap<>(); // 変換辞書

        return measure(() -> {
            Flowable
              .fromArray(args)
              //.flatMap(x -> Flowable.just(x).subscribeOn(Schedulers.from(es)).map(processor::process)) // このままだと順不同になる
              .flatMap(x -> Flowable.just(x).subscribeOn(Schedulers.from(es)).doOnNext(s -> dict.put(s, processor.process(s))))
              .blockingSubscribe();
            return Arrays.stream(args).map(dict::get).collect(Collectors.joining(","));
        });
    }



    /**
     * Helidon 実装の Reactive Operators を使う
     * curl localhost:8080/reactive/helidon?str=abc,lmn,xyz
     */
    @GET @Path("/helidon") @Produces(MediaType.TEXT_PLAIN)
    public String callHelidonReactive(@QueryParam("str") String str) throws Exception {

        final String[] args = Optional.ofNullable(str).orElse(defaultStr).split(",");
        final ConcurrentHashMap<Object, String> dict = new ConcurrentHashMap<>(); // 変換辞書

        return measure(() -> {
            Multi
              .just(args)
              .flatMap(x -> Single.just(x).observeOn(es).peek(s -> dict.put(s, processor.process(s))))
              .collectList().toCompletableFuture().join(); // この結果は使わない
            return Arrays.stream(args).map(dict::get).collect(Collectors.joining(","));
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


