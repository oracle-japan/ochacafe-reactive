package oracle.demo.reactive.rs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class RsPublisher<T> implements Publisher<T> {

    private static final Logger logger = Logger.getLogger(RsPublisher.class.getName());

    @SuppressWarnings("unused")
    private final String name;
    private final List<RsSubscription<T>> subscriptions = Collections.synchronizedList(new ArrayList<>());

    public RsPublisher(String name) {
        this.name = name;
        logger.info("[" + name + "] " + "<initialized>");
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        RsSubscription<T> subscription = new RsSubscription<>(this, subscriber);
        subscriptions.add(subscription);
    }

    public void submit(T item){
        subscriptions.stream().forEach(subscription -> subscription.submit(item));
    }

    public void close(){
        subscriptions.stream().forEach(subscription -> subscription.close());
    }

}
