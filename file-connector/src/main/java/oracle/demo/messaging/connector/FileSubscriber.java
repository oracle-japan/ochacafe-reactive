package oracle.demo.messaging.connector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


public class FileSubscriber implements Subscriber<Message<?>> {

    private static Logger logger = Logger.getLogger(FileSubscriber.class.getSimpleName());

    private final Path outputDir;
    private final String prefix;

    private Subscription subscription;

    public FileSubscriber(Config config) {
        final Path outputDir = Paths.get(config.getValue("dir", String.class));
        logger.info("Output dir: " + outputDir.toString());

        final String prefix = config.getValue("prefix", String.class);
        logger.info("Prefix: " + prefix);

        this.outputDir = outputDir;
        this.prefix = Optional.ofNullable(prefix).orElse("");
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.fine("Subscribed.");
        (this.subscription = subscription).request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Message<?> message) {
        logger.fine("Doing onNext()");
        try {
            final Path outPath = Paths.get(
                    outputDir.toString(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(prefix))
                );
            final Object payload = message.getPayload();
            byte[] buf = null;
            if(payload instanceof byte[]) buf = (byte[])payload;
            else if (payload instanceof String) buf = ((String)payload).getBytes();
            else throw new RuntimeException("Unknown object type: " + payload.getClass().getName());
            Files.write(outPath, buf,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
                );
            logger.info(String.format("Message was written to %s", outPath.toString()));
            message.ack();
        }catch(Exception e) {
            subscription.cancel();
            logger.log(Level.SEVERE, "Couldn't write to file: " + e.getMessage(), e);
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.log(Level.SEVERE, "Error - " + t.getMessage(), t);
    }

    @Override
    public void onComplete() {
        logger.info("Completed.");
    }
}
