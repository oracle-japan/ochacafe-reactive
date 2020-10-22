package oracle.demo.messaging.connector;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.eclipse.microprofile.reactive.messaging.Message;

public abstract class FileMessage<T> implements Message<T>{
    private static Logger logger = Logger.getLogger(FileMessage.class.getName());

    private final T payload;
    private final Supplier<CompletionStage<Void>> ackSupplier;

    public FileMessage(T payload){
        this(payload, null);
    }

    public FileMessage(T payload, Supplier<CompletionStage<Void>> ackSupplier){
        this.payload = payload;
        this.ackSupplier = Optional.ofNullable(ackSupplier).orElse(() -> {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.complete(null);
            return f;
        });
    }

    @Override
    public CompletionStage<Void> ack(){
        logger.fine("ack(): " + this);
        return ackSupplier.get();
    }

	@Override
	public T getPayload() {
		return payload;
    }


}