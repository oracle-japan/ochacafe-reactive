package oracle.demo.reactive;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.helidon.microprofile.tests.junit5.AddConfig;
import io.helidon.microprofile.tests.junit5.HelidonTest;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@HelidonTest
@AddConfig(key = "demo.processor.delay", value = "100")
public class MyTest{
    
    private final Logger logger = Logger.getLogger(MyTest.class.getName());

    @Inject private WebTarget webTarget;

    @AfterAll
    public static void afterAll(){
        try{
            TimeUnit.MILLISECONDS.sleep(1000);
        }catch(InterruptedException e){}
    }

    private final String defaultParam = "abc,lmn,xyz";
    private final String defaultResult = "CBA,NML,ZYX";

    private String matchingResult(String result){
        return String.format("Result: %s - Elapsed time\\(ms\\): \\d+\\n", result);
    }

    @Test
    public void test01_AsyncTest(){
        logger.info(webTarget.getUri().toString());

        // 同期呼び出しパターン
        String response = webTarget.path("/async-test/sync").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // 非同期呼び出しパターン - java.util.stream.Stream のparallel()を使う
        response = webTarget.path("/async-test/parallel").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // 非同期呼び出しパターン - JDKのスレッドプールを使って非同期処理
        response = webTarget.path("/async-test/async1").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // 非同期呼び出しパターン - helidonのスレッドプールを使って非同期処理
        response = webTarget.path("/async-test/async2").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // 非同期呼び出しパターン - MicroProfile Fault Toleranceの非同期処理を利用
        response = webTarget.path("/async-test/async-ft").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
    }

    @Test
    public void test02_AsyncClientTest(){
        
        // JAX-RS 同期RESTクライアント
        String response = webTarget.path("/async-client/sync").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // JAX-RS 非同期RESTクライアント
        response = webTarget.path("/async-client/async").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // JAX-RS JAX-RS Reactive RESTクライアント
        response = webTarget.path("/async-client/rx").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // MicroProfile 非同期RESTクライアント
        response = webTarget.path("/async-client/mp").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
    }

    @Test
    public void test03_ReactiveTest(){
        
        // Reactive Streams インターフェースをスクラッチで実装
        String response = webTarget.path("/reactive/rs").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // Reactive Streams インターフェースをスクラッチで実装 - 並列処理
        response = webTarget.path("/reactive/rs-multi").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // java.concurrent.Flowを使った実装
        response = webTarget.path("/reactive/flow").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // java.concurrent.Flowを使い、並列処理
        response = webTarget.path("/reactive/flow-multi").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // MicroProfile Reactive Streams Operators で実装
        response = webTarget.path("/reactive/operators").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // Helidon 実装の ReactiveOperators を使用
        response = webTarget.path("/reactive/helidon").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // RxJava を使う - flatMapを使って並列処理にする
        response = webTarget.path("/reactive/rxjava").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));
        
        // MicroProfile Reactive Streams Messagning で実装
        response = webTarget.path("/reactive-messaging/basic").queryParam("str", defaultParam).request().get(String.class);
        assertTrue(response.matches(matchingResult(defaultResult)));

        // MicroProfile Reactive Streams Messagning - Processor でチャネルを連結 : GET
        response = webTarget.path("/reactive-messaging/process").path("key1").queryParam("value", "val1").request().get(String.class);
        assertEquals("1LAV=1YEK", response);
        
        // MicroProfile Reactive Streams Messagning - Processor でチャネルを連結 : POST
        response = webTarget.path("/reactive-messaging/process").request()
            .post(Entity.entity("{\"key\":\"key1\",\"value\":\"val1\"}", MediaType.APPLICATION_JSON), String.class);
        assertEquals("1LAV=1YEK", response);

    }


}
