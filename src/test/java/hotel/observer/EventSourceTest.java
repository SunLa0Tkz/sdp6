package hotel.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventSourceTest {
    enum Kind {RED, BLUE}

    record Ping(Kind type, int number) implements Event<Kind> {
    }

    static class PingSource extends EventSource<Kind, Ping> {
        void send(Kind kind, int number) {
            publish(new Ping(kind, number));
        }
    }

    private final PingSource source = new PingSource();
    private final List<Integer> received = new ArrayList<>();
    private final Observer<Ping> recorder = ping -> received.add(ping.number());

    @Test
    void deliversOnlySubscribedType() {
        source.subscribe(Kind.RED, recorder);

        source.send(Kind.RED, 1);
        source.send(Kind.BLUE, 2);

        assertEquals(List.of(1), received);
    }

    @Test
    void publishWithoutListenersDoesNothing() {
        source.send(Kind.RED, 1);

        assertEquals(List.of(), received);
    }

    @Test
    void unsubscribedListenerGetsNothing() {
        source.subscribe(Kind.RED, recorder);
        source.send(Kind.RED, 1);

        source.unsubscribe(Kind.RED, recorder);
        source.send(Kind.RED, 2);

        assertEquals(List.of(1), received);
    }

    @Test
    void subscribingTwiceDeliversOnce() {
        source.subscribe(Kind.RED, recorder);
        source.subscribe(Kind.RED, recorder);

        source.send(Kind.RED, 1);

        assertEquals(List.of(1), received);
    }

    @Test
    void listenerCanLeaveFromItsHandlerWithoutSkippingOthers() {
        Observer<Ping> oneShot = new Observer<>() {
            @Override
            public void onEvent(Ping ping) {
                received.add(-ping.number());
                source.unsubscribe(Kind.RED, this);
            }
        };
        source.subscribe(Kind.RED, oneShot);
        source.subscribe(Kind.RED, recorder);

        source.send(Kind.RED, 1);
        source.send(Kind.RED, 2);

        assertEquals(List.of(-1, 1, 2), received);
    }

    @Test
    void listenerJoinedFromHandlerStartsWithNextEvent() {
        source.subscribe(Kind.RED, ping -> source.subscribe(Kind.RED, recorder));

        source.send(Kind.RED, 1);
        source.send(Kind.RED, 2);

        assertEquals(List.of(2), received);
    }

    @Test
    void failingListenerDoesNotStopTheOthers() {
        source.subscribe(Kind.RED, ping -> {
            throw new IllegalStateException("broken on purpose");
        });
        source.subscribe(Kind.RED, recorder);

        source.send(Kind.RED, 1);

        assertEquals(List.of(1), received);
    }
}
