package oracle.demo.messaging.processor;

import java.util.concurrent.CompletionStage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;

public class KeyValueMessage implements Message<KeyValueMessage.KeyValue>{

	private final KeyValue payload;
	private final Supplier<CompletionStage<Void>> ack;

	private KeyValueMessage(KeyValueMessage.KeyValue payload, Supplier<CompletionStage<Void>> ack){
		this.payload = payload;
        this.ack = Optional.ofNullable(ack).orElse(() -> {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.complete(null);
            return f;
        });
	}

	private KeyValueMessage(KeyValueMessage.KeyValue payload){
		this(payload, null);
	}

	public static KeyValueMessage of(KeyValueMessage.KeyValue payload){
		return new KeyValueMessage(payload);
	}

	public static KeyValueMessage of(KeyValueMessage.KeyValue payload, Supplier<CompletionStage<Void>> ack){
		return new KeyValueMessage(payload, ack);
	}

	@Override
	public KeyValueMessage.KeyValue getPayload() {
		return payload;
	}

    @Override
    public CompletionStage<Void> ack(){
        return ack.get();
	}
		
	public static class KeyValue {
		private String key;
		private String value;
	
		public KeyValue() {}
	
		public KeyValue(String key, String value){
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		public String toString(){
			return String.format("[key=%s, value=%s]", key, value);
		}
	}

    
}