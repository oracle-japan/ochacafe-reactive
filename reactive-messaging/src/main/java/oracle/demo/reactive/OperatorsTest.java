package oracle.demo.reactive;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.microprofile.reactive.streams.operators.CompletionRunner;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

public class OperatorsTest {

    public static void main(String[] args) {

        PublisherBuilder<Integer> evenIntsPublisher = 
            ReactiveStreams.of(1, 2, 3, 4)
            .filter(i -> i % 2 == 0);

        SubscriberBuilder<Integer, List<Integer>> doublingSubscriber = 
            ReactiveStreams.<Integer>builder()
            .map(i -> i = i * 2)
            .peek(System.out::println)
            .toList();

        CompletionRunner<List<Integer>> result = 
            evenIntsPublisher
            .to(doublingSubscriber);

        result.run().thenAccept(System.out::println);


        ReactiveStreams
        .fromIterable(() -> IntStream.range(1, 1000).boxed().iterator())
        .filter(i -> (i & 1) == 1)
        .map(i -> i * 2)
        .collect(Collectors.reducing((i, j) -> i + j))
        .run()
        .thenAccept(System.out::println);

    }

}