package hotel.listeners;

import hotel.observer.Event;

public record RevenueEvent(RevenueEventType type, long total, long processedBookings)
        implements Event<RevenueEventType> {
}
