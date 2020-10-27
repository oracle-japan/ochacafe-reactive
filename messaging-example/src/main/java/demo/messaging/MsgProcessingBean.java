package demo.messaging;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import demo.messaging.KeyValueMessage.KeyValue;
import io.helidon.common.configurable.ThreadPoolSupplier;

@ApplicationScoped
public class MsgProcessingBean {
    private final static Logger logger = Logger.getLogger(MsgProcessingBean.class.getSimpleName());

    // usging ForkJoinPool.commonPool() implicitly
    // private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>();

    // using helidon's thread pool
    private final SubmissionPublisher<KeyValueMessage> publisher = new SubmissionPublisher<>(
        ThreadPoolSupplier.builder().threadNamePrefix("messaging-").build().get(),
        Flow.defaultBufferSize()
    );

    @Outgoing("test-channel")
    public Publisher<KeyValueMessage> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }

    @Incoming("test-channel")
    public void consume(KeyValueMessage message) {
        /* DO AN ACTUAL JOB HERE!! */
        KeyValue kv = message.getPayload();
        logger.info(String.format("Consuming Message: key=%s, value=%s", kv.getKey(), kv.getValue()));
        try{
            TimeUnit.MILLISECONDS.sleep(2000);
        }catch(InterruptedException e){}
    }

    public int submit(KeyValueMessage message){
        return publisher.submit(message);
    }

    public void close(){
        publisher.close();
    }

}
