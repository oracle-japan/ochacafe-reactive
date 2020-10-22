package oracle.demo.messaging.connector;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

@ApplicationScoped
@Connector("file-connector")
public class FileConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {

    @Override 
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
        final FileSubscriber subscriber = new FileSubscriber(config);
        return ReactiveStreams.fromSubscriber(subscriber);
    }

    @Override 
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
        final FilePublisher publisher = new FilePublisher(config);
        return ReactiveStreams.fromPublisher(publisher);
    }
}