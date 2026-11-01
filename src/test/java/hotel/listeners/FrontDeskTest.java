package hotel.listeners;

import hotel.booking.Booking;
import hotel.booking.BookingEvent;
import hotel.booking.BookingEventType;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.RoomStatusEvent;
import hotel.rooms.UnitStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrontDeskTest {
    private final RoomStatusBoard board = new RoomStatusBoard();
    private final FrontDesk frontDesk = new FrontDesk(board);
    private final Booking booking = new Booking("b1", "Anna", 1, "101", 200);

    @Test
    void checkInAndCheckOutMoveTheRoomStatus() {
        frontDesk.onBooking(new BookingEvent(BookingEventType.CREATED, booking));
        assertEquals(1, frontDesk.expectedArrivals());

        frontDesk.onBooking(new BookingEvent(BookingEventType.CHECKED_IN, booking));
        assertEquals(0, frontDesk.expectedArrivals());
        assertEquals(1, frontDesk.guestsInHouse());
        assertEquals(UnitStatus.OCCUPIED, board.statusOf("101"));

        frontDesk.onBooking(new BookingEvent(BookingEventType.CHECKED_OUT, booking));
        assertEquals(0, frontDesk.guestsInHouse());
        assertEquals(UnitStatus.DIRTY, board.statusOf("101"));
    }

    @Test
    void tracksUnitsReadyForGuests() {
        frontDesk.onRoomStatus(new RoomStatusEvent(UnitStatus.FREE, 1, "101"));
        frontDesk.onRoomStatus(new RoomStatusEvent(UnitStatus.FREE, 1, "102"));
        frontDesk.onRoomStatus(new RoomStatusEvent(UnitStatus.OCCUPIED, 1, "102"));

        assertEquals(Set.of("101"), frontDesk.readyUnits());
    }
}
