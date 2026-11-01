package hotel.observer;

public interface Subject<T, E extends Event<T>> {
    void subscribe(T type, Observer<E> observer);

    void unsubscribe(T type, Observer<E> observer);
}
