package oracle.demo.messaging.connector;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;


public class OutFileMessage<T> extends FileMessage<T>{

    public OutFileMessage(T payload){
        this(payload, null);
    }

    public OutFileMessage(T payload, Supplier<CompletionStage<Void>> ackSupplier){
        super(payload, ackSupplier);
    }

    public static <T> OutFileMessage<T> of(T payload){
        return new OutFileMessage<>(payload);
    }

    public static <T> OutFileMessage<T> of(T payload, Supplier<CompletionStage<Void>> ackSupplier){
        return new OutFileMessage<>(payload, ackSupplier);
    } 
   
}