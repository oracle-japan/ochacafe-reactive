package oracle.demo.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

@ApplicationScoped
public class Processor {

    private static Logger logger = Logger.getLogger(Processor.class.getSimpleName());

    /**
     * シンプルバージョン 
     */
    public String process(String s) {
        final String result = new StringBuilder(s).reverse().toString().toUpperCase();
        logger.info("process() >> " + s + " -> " + result);

        // 意図的に3秒間スリープ
        try {
            TimeUnit.SECONDS.sleep(3);
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


}