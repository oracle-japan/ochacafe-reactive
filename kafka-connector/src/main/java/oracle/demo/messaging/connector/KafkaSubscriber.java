package oracle.demo.messaging.connector;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.helidon.messaging.connectors.kafka.KafkaMessage;

@ApplicationScoped
public class KafkaSubscriber {
    private final static Logger logger = Logger.getLogger(KafkaSubscriber.class.getName());

    @Incoming("kafka-sub")
    public void consume(KafkaMessage<String, String> message) {
        logger.info("Kafka message received: " + message.getPayload());
    }

}
