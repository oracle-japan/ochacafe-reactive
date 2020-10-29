package oracle.demo.reactive.rs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class RsSubscription<T> implements Subscription {

    private static final Logger logger = Logger.getLogger(RsSubscription.class.getName());

    @SuppressWarnings("unused")
    private final RsPublisher<T> publisher;
    private final Subscriber<? super T> subscriber;
    private final ExecutorService es;
    private boolean completed;
    private long capacity = 0;

	public RsSubscription(RsPublisher<T> publisher, Subscriber<? super T> subscriber){
        this.publisher = publisher;
        this.subscriber = subscriber;
        es = Executors.newSingleThreadExecutor(runnable -> 
            new Thread(null, runnable, subscriber.toString().replaceAll(".*\\.", "")));
        subscriber.onSubscribe(this);
    }

    public void submit(T item) {
        if(completed){
            throw new RuntimeException("Already completed.");
        }
        if(capacity <= 0){
            throw new RuntimeException("No Capacity right now.");
        }
        es.submit(() -> subscriber.onNext(item));
        capacity--;
    }  

    public void close(){
        logger.info("<close>");
        completed = true;
        es.submit(() -> subscriber.onComplete());
    }

    @Override
    public void request(long n) {
        logger.info("<request> " + n);
        if(n < 0) {
            es.submit(() -> subscriber.onError(new IllegalArgumentException("Bad request: " + n)));
            logger.warning("Closing subscription due to the bad request: " + n);
            close();
        }else{
            capacity = n;
        }
    }

    @Override
    public void cancel() {
        logger.info("<cancel>");
        completed = true;
    }


    
}
