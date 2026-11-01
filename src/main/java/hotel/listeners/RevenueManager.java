package hotel.listeners;

import hotel.booking.BookingEvent;
import hotel.booking.BookingEventType;
import hotel.observer.EventSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Middle of the observer chain: listens to bookings, republishes aggregated revenue.
public class RevenueManager extends EventSource<RevenueEventType, RevenueEvent> {
    private final int reportEvery;
    private final long target;
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong processed = new AtomicLong();
    private final AtomicBoolean targetReached = new AtomicBoolean();

    public RevenueManager(int reportEvery, long target) {
        if (reportEvery <= 0) {
            throw new IllegalArgumentException("reportEvery must be positive");
        }
        this.reportEvery = reportEvery;
        this.target = target;
    }

    public void onBooking(BookingEvent event) {
        long amount = event.booking().amount();
        // total goes first: the report with the highest count then has the full sum
        long newTotal = total.addAndGet(event.type() == BookingEventType.CANCELLED ? -amount : amount);
        long count = processed.incrementAndGet();

        if (count % reportEvery == 0) {
            publish(new RevenueEvent(RevenueEventType.REVENUE_UPDATED, total.get(), count));
        }
        if (newTotal >= target && targetReached.compareAndSet(false, true)) {
            publish(new RevenueEvent(RevenueEventType.TARGET_REACHED, newTotal, count));
        }
    }

    public long total() {
        return total.get();
    }
}
