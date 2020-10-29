package oracle.demo.messaging.processor;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import io.helidon.common.configurable.ThreadPoolSupplier;
import oracle.demo.common.Processor;
import oracle.demo.messaging.processor.KeyValueMessage.KeyValue;

/**
 * Reactive Messaging - Processor
 */
@ApplicationScoped
public class MsgProcessingBean {
    private final static Logger logger = Logger.getLogger(MsgProcessingBean.class.getName());

    // using helidon's thread pool
    private final SubmissionPublisher<KeyValueMessage> publisher = new SubmissionPublisher<>(
        ThreadPoolSupplier.builder().threadNamePrefix("messaging-process-").build().get(),
        Flow.defaultBufferSize()
    );

    @Inject private Processor processor;


    public int submit(KeyValue kv){
        return publisher.submit(KeyValueMessage.of(kv));
    }

    @Outgoing("channel-1")
    public Publisher<KeyValueMessage> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }

    @Incoming("channel-1")
    @Outgoing("channel-2")
    public KeyValue process(KeyValueMessage message) {
        logger.info("Processing [channel-1 -> channel-2]: " + message.getPayload());
        return message.getPayload();
    }

    @Incoming("channel-2")
    public void consume(KeyValue kv) {
        logger.info(String.format("Consuming [channel-2]: %s", kv));
        String response = processor.process(kv.getKey() + "=" + kv.getValue());
        kv.setResponse(response);
    }

}
