package hotel.booking;

import hotel.observer.EventSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingSystem extends EventSource<BookingEventType, BookingEvent> {
    private final Map<String, Booking> active = new ConcurrentHashMap<>();

    public void create(Booking booking) {
        if (active.putIfAbsent(booking.id(), booking) != null) {
            throw new IllegalArgumentException("Booking already exists: " + booking.id());
        }
        publish(new BookingEvent(BookingEventType.CREATED, booking));
    }

    public void cancel(String bookingId) {
        publish(new BookingEvent(BookingEventType.CANCELLED, close(bookingId)));
    }

    public void checkIn(String bookingId) {
        Booking booking = active.get(bookingId);
        if (booking == null) {
            throw unknown(bookingId);
        }
        publish(new BookingEvent(BookingEventType.CHECKED_IN, booking));
    }

    public void checkOut(String bookingId) {
        publish(new BookingEvent(BookingEventType.CHECKED_OUT, close(bookingId)));
    }

    private Booking close(String bookingId) {
        Booking booking = active.remove(bookingId);
        if (booking == null) {
            throw unknown(bookingId);
        }
        return booking;
    }

    private IllegalArgumentException unknown(String bookingId) {
        return new IllegalArgumentException("No active booking: " + bookingId);
    }
}
