package oracle.demo.messaging.basic;

import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import io.helidon.common.configurable.ThreadPoolSupplier;
import oracle.demo.reactive.FlowMessage;

@ApplicationScoped
public class PublisherBean {
    private final static Logger logger = Logger.getLogger(PublisherBean.class.getSimpleName());

    ///// 実装方法 その1

    // using helidon's thread pool
    private final SubmissionPublisher<FlowMessage> publisher = new SubmissionPublisher<>(
        ThreadPoolSupplier.builder().threadNamePrefix("messaging-").build().get(),
        Flow.defaultBufferSize()
    );

    public int submit(FlowMessage message){
        return publisher.submit(message);
    }

    // このメソッドは起動時に一回だけ呼び出される
    @Outgoing("messaging-demo")
    public Publisher<FlowMessage> preparePublisher() {
        logger.info("@Outgoing(\"messaging-demo\")");
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }


    ///// 実装方法 その2

    private final LinkedBlockingDeque<FlowMessage> queue = new LinkedBlockingDeque<>();

    public void submit2(FlowMessage messsage){
        try{
            queue.put(messsage);
        }catch(Exception e){ throw new RuntimeException("cannot submit message: " + e.getMessage(), e);}
    }

    //@Outgoing("messaging-demo")
    public FlowMessage publish2() {
        logger.info("@Outgoing(\"messaging-demo\")");
        try{
            return queue.take(); // キューにデータが存在するまでブロックする
        }catch(Exception e){ throw new RuntimeException("cannot get message from the queue", e);}
    }


}
