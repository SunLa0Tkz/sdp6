package hotel.simulation;

import hotel.booking.Booking;
import hotel.booking.BookingSystem;

import java.util.List;

// Producer: guests booking the units of one floor. Every fifth booking gets cancelled.
public class BookingTraffic implements Runnable {
    private final BookingSystem bookings;
    private final String name;
    private final int floor;
    private final List<String> units;
    private final int count;

    public BookingTraffic(BookingSystem bookings, String name, int floor, List<String> units, int count) {
        this.bookings = bookings;
        this.name = name;
        this.floor = floor;
        this.units = List.copyOf(units);
        this.count = count;
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            String id = name + "-" + i;
            String unit = units.get(i % units.size());
            bookings.create(new Booking(id, "Guest " + id, floor, unit, 100 + 50L * (i % 3)));
            if (i % 5 == 4) {
                bookings.cancel(id);
            } else {
                bookings.checkIn(id);
                bookings.checkOut(id);
            }
        }
    }
}
