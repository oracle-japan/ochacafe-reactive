package oracle.demo.reactive.rs;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class RsSubscriber<T> implements Subscriber<T> {

    private final static Logger logger = Logger.getLogger(RsSubscriber.class.getName());
    private final static long defaultBufferSize = 256;

    private final String name;
    private final Consumer<? super T> consumer;
    private final long bufferSize;
    private Subscription subscription;
    private long count;


    RsSubscriber(String name, long bufferSize, Consumer<? super T> consumer) {
        this.name = name;
        this.bufferSize = bufferSize;
        this.consumer = consumer;
        logger.info("[" + name + "] " + "<initialized>");
    }

    RsSubscriber(String name, Consumer<? super T> consumer) {
        this(name, defaultBufferSize, consumer);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.info("[" + name + "] " + "<onSubscribe>");

        long initialRequestSize = bufferSize;
        count = bufferSize - bufferSize / 2; // re-request when half consumed
        (this.subscription = subscription).request(initialRequestSize);
    }

    @Override
    public void onNext(T item) {
        logger.info("[" + name + "] " + "<onNext> " + item.toString());

        if (--count <= 0)
            subscription.request(count = bufferSize - bufferSize / 2);
        consumer.accept(item);
    }

    @Override
    public void onError(Throwable ex) {
        logger.log(Level.WARNING , "[" + name + "] " + "<onError> " + ex.getMessage(), ex);
    }

    @Override
    public void onComplete() {
        logger.info("[" + name + "] " + "<onComplete>");
    }
    
}
