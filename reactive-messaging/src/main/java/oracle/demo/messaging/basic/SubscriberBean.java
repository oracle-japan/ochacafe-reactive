package oracle.demo.messaging.basic;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Subscriber;

import oracle.demo.common.Processor;
import oracle.demo.reactive.FlowMessage;
import oracle.demo.reactive.FlowSubscriber;

@ApplicationScoped
public class SubscriberBean {
    private final static Logger logger = Logger.getLogger(SubscriberBean.class.getName());

    @Inject private Processor processor;

    // 処理を直接記述する方法
    // このメッセージはonNext()のタイミングで毎回呼び出される
    @Incoming("messaging-test-1")
    public void consume(FlowMessage message) {
        logger.info("@Incoming(\"messaging-test-1\")");
        try{
            final String response = processor.process(message.getRequest()); 
            message.complete(response); // メッセージに実行結果をセット
        }catch(Throwable t){
            message.completeExceptionally(t); // メッセージに実行例外をセット
        }
    }



    // 処理を直接記述せずに、Subscriberを指定する方法
    // このメソッドは起動時に一回だけ呼び出される
    // 実際の処理は、SubscriberのonNext()が行う
    //@Incoming("messaging-test-1")
    public Subscriber<FlowMessage> prepareSubscriber() {
        logger.info("@Incoming(\"messaging-test-1\")");
        return FlowAdapters.toSubscriber(new FlowSubscriber());
    }


}
