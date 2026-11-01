package hotel.listeners;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ManagerDashboard {
    private final AtomicInteger reports = new AtomicInteger();
    private final AtomicBoolean targetReached = new AtomicBoolean();
    private final AtomicReference<RevenueEvent> latest =
            new AtomicReference<>(new RevenueEvent(RevenueEventType.REVENUE_UPDATED, 0, 0));

    public void onRevenue(RevenueEvent event) {
        if (event.type() == RevenueEventType.TARGET_REACHED) {
            targetReached.set(true);
            return;
        }
        reports.incrementAndGet();
        // reports can arrive out of order from different producer threads
        latest.accumulateAndGet(event, (a, b) -> b.processedBookings() > a.processedBookings() ? b : a);
    }

    public int reports() {
        return reports.get();
    }

    public boolean targetReached() {
        return targetReached.get();
    }

    public long latestTotal() {
        return latest.get().total();
    }
}
