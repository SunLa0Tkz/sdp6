package hotel.listeners;

import hotel.booking.Booking;
import hotel.booking.BookingEvent;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.RoomStatusEvent;
import hotel.rooms.UnitStatus;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FrontDesk {
    private final RoomStatusBoard board;
    private final AtomicInteger expectedArrivals = new AtomicInteger();
    private final AtomicInteger guestsInHouse = new AtomicInteger();
    private final Set<String> readyUnits = ConcurrentHashMap.newKeySet();

    public FrontDesk(RoomStatusBoard board) {
        this.board = board;
    }

    public void onBooking(BookingEvent event) {
        Booking booking = event.booking();
        switch (event.type()) {
            case CREATED -> expectedArrivals.incrementAndGet();
            case CANCELLED -> expectedArrivals.decrementAndGet();
            case CHECKED_IN -> {
                expectedArrivals.decrementAndGet();
                guestsInHouse.incrementAndGet();
                board.update(booking.floor(), booking.unitId(), UnitStatus.OCCUPIED);
            }
            case CHECKED_OUT -> {
                guestsInHouse.decrementAndGet();
                board.update(booking.floor(), booking.unitId(), UnitStatus.DIRTY);
            }
        }
    }

    public void onRoomStatus(RoomStatusEvent event) {
        if (event.type() == UnitStatus.FREE) {
            readyUnits.add(event.unitId());
        } else {
            readyUnits.remove(event.unitId());
        }
    }

    public int expectedArrivals() {
        return expectedArrivals.get();
    }

    public int guestsInHouse() {
        return guestsInHouse.get();
    }

    public Set<String> readyUnits() {
        return Set.copyOf(readyUnits);
    }
}
