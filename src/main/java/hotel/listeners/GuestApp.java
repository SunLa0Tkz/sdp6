package hotel.listeners;

import hotel.booking.BookingEvent;
import hotel.booking.BookingEventType;
import hotel.observer.Observer;
import hotel.observer.Subject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Lives as long as one stay: subscribes to check-out only after check-in
// and leaves the subject from inside its own handler when the stay is over.
public class GuestApp implements Observer<BookingEvent> {
    private final Subject<BookingEventType, BookingEvent> bookings;
    private final String bookingId;
    private final List<String> messages = new CopyOnWriteArrayList<>();

    public GuestApp(Subject<BookingEventType, BookingEvent> bookings, String bookingId) {
        this.bookings = bookings;
        this.bookingId = bookingId;
    }

    public void connect() {
        bookings.subscribe(BookingEventType.CHECKED_IN, this);
        bookings.subscribe(BookingEventType.CANCELLED, this);
    }

    @Override
    public void onEvent(BookingEvent event) {
        if (!event.booking().id().equals(bookingId)) {
            return;
        }
        switch (event.type()) {
            case CHECKED_IN -> {
                messages.add("Welcome! Your room is " + event.booking().unitId());
                bookings.subscribe(BookingEventType.CHECKED_OUT, this);
            }
            case CHECKED_OUT -> {
                messages.add("Thank you for staying with us");
                leave();
            }
            case CANCELLED -> {
                messages.add("Your booking was cancelled");
                leave();
            }
            default -> throw new IllegalStateException("Not subscribed to " + event.type());
        }
    }

    private void leave() {
        bookings.unsubscribe(BookingEventType.CHECKED_IN, this);
        bookings.unsubscribe(BookingEventType.CHECKED_OUT, this);
        bookings.unsubscribe(BookingEventType.CANCELLED, this);
    }

    public List<String> messages() {
        return List.copyOf(messages);
    }
}
