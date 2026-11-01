package hotel.listeners;

import hotel.booking.Booking;
import hotel.booking.BookingSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestAppTest {
    private final BookingSystem bookings = new BookingSystem();
    private final GuestApp app = new GuestApp(bookings, "b1");

    @Test
    void followsItsOwnBookingOnly() {
        app.connect();
        bookings.create(new Booking("b2", "Other guest", 1, "102", 100));
        bookings.create(new Booking("b1", "Anna", 1, "101", 200));

        bookings.checkIn("b2");
        bookings.checkIn("b1");

        assertEquals(List.of("Welcome! Your room is 101"), app.messages());
    }

    @Test
    void leavesAfterCheckOut() {
        app.connect();
        Booking stay = new Booking("b1", "Anna", 1, "101", 200);
        bookings.create(stay);
        bookings.checkIn("b1");
        bookings.checkOut("b1");

        // same id again: the app has already left, so it must stay silent
        bookings.create(stay);
        bookings.checkIn("b1");

        assertEquals(List.of("Welcome! Your room is 101", "Thank you for staying with us"), app.messages());
    }

    @Test
    void leavesAfterCancellation() {
        app.connect();
        Booking stay = new Booking("b1", "Anna", 1, "101", 200);
        bookings.create(stay);
        bookings.cancel("b1");

        bookings.create(stay);
        bookings.checkIn("b1");

        assertEquals(List.of("Your booking was cancelled"), app.messages());
    }
}
