package hotel.booking;

import hotel.observer.Event;

public record BookingEvent(BookingEventType type, Booking booking) implements Event<BookingEventType> {
}
