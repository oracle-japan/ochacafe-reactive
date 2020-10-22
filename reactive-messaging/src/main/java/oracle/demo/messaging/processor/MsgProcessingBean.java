package oracle.demo.messaging.processor;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import oracle.demo.common.Processor;
import oracle.demo.messaging.processor.KeyValueMessage.KeyValue;

/**
 * Reactive Streams/Flow 関連クラスをまったく使わずに実装した例
 */
@ApplicationScoped
public class MsgProcessingBean {
    private final static Logger logger = Logger.getLogger(MsgProcessingBean.class.getName());

    private final LinkedBlockingDeque<KeyValue> queue = new LinkedBlockingDeque<>();

    @Inject private Processor processor;

    public void submit(KeyValue kv){
        try{
            Objects.requireNonNull(kv.getValue());
            queue.put(kv);        
        }catch(Exception e){ throw new RuntimeException("cannot submit message: " + kv, e);}
    }

    // Subscriberからの要求に応じて呼び出され publish するメッセージを作成する
    @Outgoing("channel-1")
    public KeyValueMessage publish() {
        logger.info("publish() is being called.");
        try{
            KeyValue kv = queue.take(); // キューにデータが存在するまでブロックする
            logger.info("Publishing [channel-1]: " + kv);
            return KeyValueMessage.of(kv);
        }catch(Exception e){ throw new RuntimeException("cannot get message from the queue", e);}
    }

    @Incoming("channel-1")
    @Outgoing("channel-2")
    public String process(KeyValueMessage message) {
        logger.info("Processing [channel-1 -> channel-2]: " + message.getPayload());
        KeyValue kv = message.getPayload();
        return processor.process(kv.getKey() + "=" + kv.getValue());
    }

    @Incoming("channel-2")
    public void consume(String message) {
        logger.info(String.format("Consuming [channel-2]: %s", message));
    }

}
