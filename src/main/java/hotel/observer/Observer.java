package hotel.observer;

public interface Observer<E> {
    void onEvent(E event);
}
