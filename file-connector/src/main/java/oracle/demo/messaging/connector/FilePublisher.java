package oracle.demo.messaging.connector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.helidon.common.configurable.ScheduledThreadPoolSupplier;
import io.helidon.common.configurable.ThreadPoolSupplier;

public class FilePublisher implements Publisher<Message<?>> {

    private static Logger logger = Logger.getLogger(FilePublisher.class.getSimpleName());

    private final Path inputDir;
    private final Path archiveDir;
    private final long pollingInterval;
    private final boolean passByReference;
    private final String contentType;

    private ConcurrentSkipListSet<Path> workingFiles = new ConcurrentSkipListSet<>();

    private final static ExecutorService executor = ThreadPoolSupplier
        .builder().threadNamePrefix("file-publisher-").build().get();
    private final static ScheduledExecutorService se = ScheduledThreadPoolSupplier
        .builder().threadNamePrefix("file-poller-").build().get();

    @SuppressWarnings("unused")
    private Subscriber<? super Message<?>> subscriber;
    private FileSubscription subscription;
    private boolean subscribed; // true after first subscribe

    public FilePublisher(Config config) {
        final long pollingInterval = config.getValue("polling-interval", Long.class);
        logger.info("polling-interval: " + pollingInterval);

        final Path inputDir = Paths.get(config.getValue("dir", String.class));
        logger.info("Input dir: " + inputDir.toString());

        final Path archiveDir = Paths.get(config.getValue("archive-dir", String.class));
        logger.info("Archive dir: " + archiveDir.toString());

        final boolean passByReference = config.getValue("pass-by-reference", Boolean.class);
        logger.info("Pass-by-reference: " + passByReference);

        final String contentType = config.getOptionalValue("content-type", String.class).orElse("bytes");
        logger.info("content-type: " + contentType);

        this.inputDir = inputDir;
        this.archiveDir = archiveDir;
        this.pollingInterval = pollingInterval;
        this.passByReference = passByReference;
        this.contentType = contentType;
    }

    @Override
    public synchronized void subscribe(Subscriber<? super Message<?>> subscriber) {
        if (subscribed)
            subscriber.onError(new IllegalStateException()); // only one subscriber is allowed to subscribe
        else {
            this.subscriber = subscriber;
            subscribed = true;
            subscription = new FileSubscription(subscriber, executor);
            subscriber.onSubscribe(subscription);
            start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                stop();
            }));
        }
    }

    public void start(){
        logger.info("Start polling...");
        se.scheduleAtFixedRate(() -> {
            logger.finest("Started polling batch.");
            if(subscribed){
                try(Stream<Path> files = Files.list(inputDir)){
                    files.forEach(path -> {
                        if(workingFiles.contains(path)){
                            logger.warning("Files is still in process: " + path.toAbsolutePath().toString());
                            return;
                        }
                        logger.info("Found new file: " + path.toAbsolutePath().toString());
                        try {
                            workingFiles.add(path);

                            // ack supplier -> move file to archive directory
                            final Supplier<CompletionStage<Void>> ackSupplier = () -> {
                                return CompletableFuture.runAsync(() -> {
                                    final Path archivePath = 
                                    Paths.get(
                                        archiveDir.toString(), 
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS-")) + path.getFileName().toString()
                                    );
                                    try{
                                        Files.move(path, archivePath);
                                        logger.info(String.format("File was archived to %s", archivePath));
                                    }catch(Exception e){
                                        logger.warning(String.format("Couldn't archive file: %s - %s", path.toAbsolutePath(), e.getMessage()));
                                        throw new RuntimeException("Couldn't archive file: " + path.toAbsolutePath(), e);
                                    }finally{
                                        workingFiles.remove(path);
                                    }
                                }, executor);
                            };

                            // send message according to content-type
                            if(passByReference){
                                subscription.submit(new InFileMessage<Path>(path, path, ackSupplier));    
                            }else{
                                if(contentType.equalsIgnoreCase("bytes")){
                                    subscription.submit(new InFileMessage<byte[]>(Files.readAllBytes(path), path, ackSupplier));    
                                }else if(contentType.equalsIgnoreCase("string")){
                                    subscription.submit(new InFileMessage<String>(new String(Files.readAllBytes(path)), path, ackSupplier));    
                                }else{
                                    throw new IllegalArgumentException("unknown content type: " + contentType);
                                }
                            }

                        } catch (Exception e) {
                            logger.warning(String.format("Couldn't submit message: %s - %s", path.toAbsolutePath(), e.getMessage()));
                            workingFiles.remove(path);
                        }
                    });
                }catch(Exception e){
                    logger.warning(String.format("Couldn't list files in %s - %s", inputDir.toAbsolutePath(), e.getMessage()));
                }
            }
            logger.finest("Finished polling batch.");
        }, pollingInterval, pollingInterval, TimeUnit.MILLISECONDS);    
    }

    public synchronized void stop(){
        subscription.close();
        se.shutdown();
        executor.shutdown();
        try {
            se.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { logger.warning("Poller termination error: " + e.getMessage()); }
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { logger.warning("Executer termination error: " + e.getMessage()); }
        logger.info("FilePublisher was stopped.");
    }





    public static class FileSubscription implements Subscription {
        private final Subscriber<? super Message<?>> subscriber;
        private final ExecutorService executor;
        private boolean completed;
        private long capacity = 0;

        FileSubscription(Subscriber<? super Message<?>> subscriber, ExecutorService executor) {
            this.subscriber = subscriber;
            this.executor = executor;
        }

        public synchronized void close(){
            executor.execute(() -> subscriber.onComplete());
            completed = true;
        }

        public synchronized void submit(Message<?> item) {
            if(completed){
                throw new RuntimeException("Already completed.");
            }
            if(capacity <= 0){
                throw new RuntimeException("No Capacity right now.");
            }
            executor.submit(() -> {
                subscriber.onNext(item);
            });
            if(capacity != Long.MAX_VALUE) capacity--;
        }

        @Override
        public synchronized void request(long n) {
            logger.info("request: " + n);
            if(n < 0) {
                executor.execute(() -> subscriber.onError(new IllegalArgumentException("Bad request: " + n)));
            }else{
                capacity = n;
            }
        }

        @Override
        public synchronized void cancel() {
            logger.info("cancel");
            completed = true;
        }
    }



}