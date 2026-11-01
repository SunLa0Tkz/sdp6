package hotel.listeners;

import hotel.booking.Booking;
import hotel.booking.BookingEventType;
import hotel.booking.BookingSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevenueChainTest {
    private final BookingSystem bookings = new BookingSystem();
    private final RevenueManager revenue = new RevenueManager(2, 500);
    private final ManagerDashboard dashboard = new ManagerDashboard();

    @BeforeEach
    void wire() {
        bookings.subscribe(BookingEventType.CREATED, revenue::onBooking);
        bookings.subscribe(BookingEventType.CANCELLED, revenue::onBooking);
        for (RevenueEventType type : RevenueEventType.values()) {
            revenue.subscribe(type, dashboard::onRevenue);
        }
    }

    private void book(String id, long amount) {
        bookings.create(new Booking(id, "Guest", 1, "101", amount));
    }

    @Test
    void cancellationTakesMoneyBack() {
        book("a", 100);
        book("b", 150);
        bookings.cancel("a");

        assertEquals(150, revenue.total());
    }

    @Test
    void dashboardGetsAReportEveryNthBooking() {
        book("a", 100);
        assertEquals(0, dashboard.reports());

        book("b", 150);
        assertEquals(1, dashboard.reports());
        assertEquals(250, dashboard.latestTotal());
    }

    @Test
    void targetIsAnnouncedOnce() {
        book("a", 100);
        assertFalse(dashboard.targetReached());

        book("b", 400);
        assertTrue(dashboard.targetReached());
    }

    @Test
    void rejectsWrongReportPeriod() {
        assertThrows(IllegalArgumentException.class, () -> new RevenueManager(0, 100));
    }
}
