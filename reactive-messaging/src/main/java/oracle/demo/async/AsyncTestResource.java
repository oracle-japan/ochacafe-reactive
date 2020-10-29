package oracle.demo.async;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
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

import io.helidon.common.configurable.ThreadPoolSupplier;
import oracle.demo.common.Processor;

@ApplicationScoped
@Path("/async-test")
public class AsyncTestResource {

    private static final Logger logger = Logger.getLogger(AsyncTestResource.class.getName());
    private final String defaultStr = "abc,lmn,xyz";

    @Inject 
    private Processor processor; // processor::process は処理に3秒かかる

    /**
     * 同期呼び出しパターン
     * curl localhost:8080/async-test/sync?str=abc,lmn,xyz
     */
    @GET @Path("/sync") @Produces(MediaType.TEXT_PLAIN)
    public String callSync(@QueryParam("str") String str) {

        return measure(() -> {
            return 
            Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(",")) // {"abc","lmn","xyz"}
            .map(processor::process) // {"CBA","NML","ZYX"}
            .collect(Collectors.joining(",")); // "CBA,NML,ZYX"
        });

    }


    /**
     * 非同期呼び出しパターン - java.util.stream.Stream のparallel()を使う
     * curl localhost:8080/async-test/parallel?str=abc,lmn,xyz
     */
    @GET @Path("/parallel") @Produces(MediaType.TEXT_PLAIN)
    public String callParallel(@QueryParam("str") String str) {
        logger.info("#processors: " + Runtime.getRuntime().availableProcessors());
        logger.info("ForkJoinPool.commonPool #Parallelism: " + ForkJoinPool.getCommonPoolParallelism());

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .parallel() // 並列処理 配列は順序付けされたstreamなので検出順序は維持される
            .map(processor::process)
            .collect(Collectors.joining(","));
        });
    }


    /**
     * 非同期呼び出しパターン - JDKのスレッドプールを使って非同期処理
     * curl "localhost:8080/async-test/async1?str=abc,lmn,xyz&nthreads=3"
     */
    @GET @Path("/async1") @Produces(MediaType.TEXT_PLAIN)
    public String callAsync1(@QueryParam("str") String str, @QueryParam("nthreads") String nthreads) {

        final int nThreads = Integer.parseInt(Optional.ofNullable(nthreads).orElse("3")); // デフォルト=3
        logger.info("#Threads: " + nThreads);
        final ExecutorService ex = Executors.newFixedThreadPool(nThreads); // 本来はもっと長いライフタイム
        try{
            return measure(() -> {
                return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                    .map(x -> CompletableFuture.supplyAsync(() -> { // Future f = ex1.submit()でもいけるけど...
                        return processor.process(x);    
                    }, ex)) // {CompletableFuture, CompletableFuture, CompletableFuture}
                    // 一旦collectしないと個々に後続のmap処理(joinでブロックされる)まで流れてしまい、結果並列処理にならない
                    .collect(Collectors.toList()) // List<CompletableFuture>
                    .stream()
                    .map(CompletableFuture::join) // 実行結果を取得　{"CBA","NML","ZYX"}
                    .collect(Collectors.joining(","));
            });
        }finally{
            ex.shutdown();
            try { ex.awaitTermination(1, TimeUnit.SECONDS); } catch(InterruptedException e){}
        }

    }


    /**
     * 非同期呼び出しパターン - helidonのスレッドプールを使って非同期処理
     * curl localhost:8080/async-test/async2?str=abc,lmn,xyz
     */
    private final ExecutorService ex = ThreadPoolSupplier.builder()
        .threadNamePrefix("helidon-pool-").build().get();  // helidonが提供するスレッドプール

    @GET @Path("/async2") @Produces(MediaType.TEXT_PLAIN)
    public String callAsync2(@QueryParam("str") String str) {

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
                .map(x -> CompletableFuture.supplyAsync(() -> {
                    return processor.process(x);    
                }, ex))
                .collect(Collectors.toList()) // List<CompletableFuture>
                .stream()
                .map(CompletableFuture::join) // 実行結果を取得
                .collect(Collectors.joining(","));
        });
    
    }


    /**
     * 非同期呼び出しパターン - MicroProfile Fault Toleranceの非同期処理を利用
     * @Asynchrnous アノテーションのついたmethodを呼び出す
     * curl localhost:8080/async-test/async-ft?str=abc,lmn,xyz
     */
    @GET @Path("/async-ft") @Produces(MediaType.TEXT_PLAIN)
    public String callAsyncFT(@QueryParam("str") String str) {

        return measure(() -> {
            return Arrays.stream(Optional.ofNullable(str).orElse(defaultStr).split(","))
            .map(processor::processAsync) // CompletionStage
            .collect(Collectors.toList()) // List<CompletionStage>
            .stream()
            .map(f -> ((CompletableFuture<String>)f).join()) // 実行結果を取得
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


