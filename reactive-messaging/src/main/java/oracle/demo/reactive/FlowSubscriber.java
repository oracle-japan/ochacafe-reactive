package oracle.demo.reactive;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import oracle.demo.common.Processor;

public class FlowSubscriber implements Subscriber<FlowMessage> {

    private static Logger logger = Logger.getLogger(FlowSubscriber.class.getName());

    private final Processor processor = CDI.current().select(Processor.class).get();
    
    @SuppressWarnings("unused")
    private Subscription subscription;

    public FlowSubscriber() {
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.info(this + " onSubscribe()");
        (this.subscription = subscription).request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(FlowMessage message) {
        logger.info(this + " onNext()");
        try{
            final String response = processor.process(message.getRequest()); 
            message.complete(response); // メッセージに実行結果をセット
        }catch(Throwable t){
            message.completeExceptionally(t); // メッセージに実行例外をセット
        }finally{
            //subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.info(this + " onError()");
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        logger.info(this + " onComplete()");
    }
}
