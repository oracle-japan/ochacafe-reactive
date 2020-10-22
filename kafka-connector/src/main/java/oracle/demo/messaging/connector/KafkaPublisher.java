package oracle.demo.messaging.connector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.helidon.messaging.connectors.kafka.KafkaMessage;

@ApplicationScoped
public class KafkaPublisher {
    private final static Logger logger = Logger.getLogger(KafkaPublisher.class.getName());

    private final LinkedBlockingDeque<KafkaMessage<String, String>> queue = new LinkedBlockingDeque<>();

    public synchronized void submit(String s){
        try{
            final CompletableFuture<Void> acked = new CompletableFuture<>();
            KafkaMessage<String, String> message = KafkaMessage.of(s, () -> {
                logger.info("ack()");
                acked.complete(null);
                return new CompletableFuture<Void>();
            });
            queue.put(message);
            acked.get(); // オーバーランしないように意図的にack()が返るまでブロックする 
        }catch(Exception e){ throw new RuntimeException("Couldn't submit message: " + s, e);}
    }

    @Outgoing("kafka-pub")
    public KafkaMessage<String, String> publish() {
        try{
            KafkaMessage<String, String> message = queue.take(); // キューにデータが入るまでブロックされる
            logger.info("Publishing Kafka message: " + message.getPayload());
            return message;
        }catch(Exception e){ throw new RuntimeException("Couldn't get message from the queue", e);}
    }

}
