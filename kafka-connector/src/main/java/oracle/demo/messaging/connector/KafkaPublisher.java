package oracle.demo.messaging.connector;

import java.util.concurrent.CompletableFuture;
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
import io.helidon.messaging.connectors.kafka.KafkaMessage;

@ApplicationScoped
public class KafkaPublisher {
    private final static Logger logger = Logger.getLogger(KafkaPublisher.class.getName());

    private final SubmissionPublisher<KafkaMessage<String, String>> publisher = new SubmissionPublisher<>(
        ThreadPoolSupplier.builder().threadNamePrefix("kafka-pub-").build().get(),
        Flow.defaultBufferSize()
    );

    private long id = 0; 

    public void submit(String message) {
        final Long key = Long.valueOf(++id); 
        logger.fine(String.format("Sending message: %s(%d)", message, key));
        logger.fine("Estimated maximum lag: " + publisher.estimateMaximumLag());

        final long estimateMinimumDemand = publisher.estimateMinimumDemand();
        logger.fine("Estimated minimum demand: " + estimateMinimumDemand);
        if(estimateMinimumDemand <= 0){
            logger.fine(String.format("You are sending a message while estimateMinimumDemand is %d which is <= 0.", estimateMinimumDemand));
        }

        publisher.submit(KafkaMessage.of(key.toString(), message, () -> {
            logger.fine(String.format("Ack received: %s(%d)", message, key));
            return CompletableFuture.completedFuture(null);
        }));
    }

    // Kafka Connector
    // based on MicroProfile Reactive Streams Messaging
    @Outgoing("kafka-pub")
    public Publisher<KafkaMessage<String, String>> preparePublisher() {
        return ReactiveStreams
                .fromPublisher(FlowAdapters.toPublisher(publisher))
                .buildRs();
    }

}
