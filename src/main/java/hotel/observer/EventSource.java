package hotel.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class EventSource<T, E extends Event<T>> implements Subject<T, E> {
    private final Map<T, CopyOnWriteArrayList<Observer<E>>> observers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(T type, Observer<E> observer) {
        observers.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(T type, Observer<E> observer) {
        CopyOnWriteArrayList<Observer<E>> list = observers.get(type);
        if (list != null) {
            list.remove(observer);
        }
    }

    // Iterates over a snapshot, so a listener can leave or join from its own handler.
    // A publish already running on another thread may still deliver one last event
    // to a listener that has just left.
    protected void publish(E event) {
        CopyOnWriteArrayList<Observer<E>> list = observers.get(event.type());
        if (list == null) {
            return;
        }
        for (Observer<E> observer : list) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException e) {
                // one broken listener must not take the event away from the others
                System.err.println("Listener failed on " + event + ": " + e);
            }
        }
    }
}
