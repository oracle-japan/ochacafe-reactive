package oracle.demo.messaging.basic;

import java.util.concurrent.Flow;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import io.helidon.common.configurable.ThreadPoolSupplier;
import oracle.demo.reactive.FlowMessage;

@ApplicationScoped
public class PublisherBean {
    private final static Logger logger = Logger.getLogger(PublisherBean.class.getName());

    ///// 実装方法 その1

    // using helidon's thread pool
    private final SubmissionPublisher<FlowMessage> publisher = new SubmissionPublisher<>(
        ThreadPoolSupplier.builder().threadNamePrefix("messaging-").build().get(),
        Flow.defaultBufferSize()
    );

    public int submit(FlowMessage message){
        return publisher.submit(message);
    }

    @Outgoing("messaging-test-1")
    public Publisher<FlowMessage> preparePublisher() {
        logger.info("@Outgoing(\"messaging-test-1\")");
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }

/*
    ///// 実装方法 その2

    private final LinkedBlockingDeque<FlowMessage> queue = new LinkedBlockingDeque<>();

    public void submit(FlowMessage messsage){
        try{
            queue.put(messsage);
        }catch(Exception e){ throw new RuntimeException("cannot submit message: " + e.getMessage(), e);}
    }

    @Outgoing("messaging-test-1")
    public FlowMessage publish() {
        logger.info("@Outgoing(\"messaging-test-1\")");
        try{
            return queue.take(); // キューにデータが存在するまでブロックする
        }catch(Exception e){ throw new RuntimeException("cannot get message from the queue", e);}
    }
*/

}
