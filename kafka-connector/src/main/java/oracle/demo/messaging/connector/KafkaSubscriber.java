package oracle.demo.messaging.connector;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.helidon.messaging.connectors.kafka.KafkaMessage;

@ApplicationScoped
public class KafkaSubscriber {
    private final static Logger logger = Logger.getLogger(KafkaSubscriber.class.getSimpleName());

    /*
    @Incoming("kafka-sub")
    public void consume(KafkaMessage<String, String> message) {
        logger.info("Kafka message received: " + message.getPayload());
    }
    */

    @Incoming("kafka-sub")
    public Subscriber<KafkaMessage<String, String>> consume() {
        //logger.info("Kafka message received: " + message.getPayload());
        return new Subscriber<KafkaMessage<String, String>>(){

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription){
                logger.info("onSubscription()");
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }
            @Override
            public void onNext(KafkaMessage<String, String> message) {
                logger.info("Kafka message received: " + message.getPayload());
            }
            @Override
            public void onComplete() {
                logger.info("onComplete()");
            }
            @Override
            public void onError(Throwable t) {
                logger.info("onError() - " + t.getMessage());
            }
        };
    }


}
