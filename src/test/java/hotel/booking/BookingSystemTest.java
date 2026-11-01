package hotel.booking;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingSystemTest {
    private final BookingSystem bookings = new BookingSystem();
    private final Booking booking = new Booking("b1", "Anna", 1, "101", 200);

    @Test
    void publishesEachStepOfAStay() {
        List<BookingEventType> seen = new ArrayList<>();
        for (BookingEventType type : BookingEventType.values()) {
            bookings.subscribe(type, event -> seen.add(event.type()));
        }

        bookings.create(booking);
        bookings.checkIn("b1");
        bookings.checkOut("b1");

        assertEquals(List.of(BookingEventType.CREATED, BookingEventType.CHECKED_IN, BookingEventType.CHECKED_OUT), seen);
    }

    @Test
    void rejectsDuplicateId() {
        bookings.create(booking);

        assertThrows(IllegalArgumentException.class, () -> bookings.create(booking));
    }

    @Test
    void closedBookingCannotBeUsedAgain() {
        bookings.create(booking);
        bookings.cancel("b1");

        assertThrows(IllegalArgumentException.class, () -> bookings.checkIn("b1"));
        assertThrows(IllegalArgumentException.class, () -> bookings.checkOut("b1"));
    }
}
