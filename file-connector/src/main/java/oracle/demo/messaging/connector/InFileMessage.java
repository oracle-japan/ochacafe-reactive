package oracle.demo.messaging.connector;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;


public class InFileMessage<T> extends FileMessage<T>{

    private final Path source;

    public InFileMessage(T payload, Path source){
        this(payload, source, null);
    }

    public InFileMessage(T payload, Path source, Supplier<CompletionStage<Void>> ackSupplier){
        super(payload, ackSupplier);
        this.source = source;
    }

    public Path getSource() {
        return source;
    }
 
    public File getSourceFile(){
        return source.toFile();
    }

    public static <T> InFileMessage<T> of(T payload, Path source){
        return new InFileMessage<>(payload, source);
    }

    public static <T> InFileMessage<T> of(T payload, Path source, Supplier<CompletionStage<Void>> ackSupplier){
        return new InFileMessage<>(payload, source, ackSupplier);
    }  

   
}