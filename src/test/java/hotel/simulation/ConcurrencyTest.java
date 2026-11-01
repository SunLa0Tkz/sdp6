package hotel.simulation;

import hotel.booking.Booking;
import hotel.booking.BookingEvent;
import hotel.booking.BookingEventType;
import hotel.booking.BookingSystem;
import hotel.integration.CleaningDispatcher;
import hotel.integration.CleaningPlanEventType;
import hotel.listeners.Housekeeping;
import hotel.listeners.ManagerDashboard;
import hotel.listeners.RevenueEventType;
import hotel.listeners.RevenueManager;
import hotel.observer.Observer;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Hotel;
import hotel.tree.HotelElement;
import hotel.tree.Room;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {
    private static final int THREADS = 4;
    private static final int EVENTS_PER_THREAD = 1000;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private void bookFromAllThreads(ProducerPool pool, BookingSystem bookings) {
        for (int t = 0; t < THREADS; t++) {
            String prefix = "t" + t + "-";
            pool.submit(() -> {
                for (int i = 0; i < EVENTS_PER_THREAD; i++) {
                    bookings.create(new Booking(prefix + i, "Guest", 1, "101", 10));
                }
            });
        }
    }

    @Test
    void stressNoEventIsLost() {
        BookingSystem bookings = new BookingSystem();
        AtomicInteger received = new AtomicInteger();
        bookings.subscribe(BookingEventType.CREATED, event -> received.incrementAndGet());

        ProducerPool pool = new ProducerPool(THREADS);
        bookFromAllThreads(pool, bookings);

        assertTrue(pool.shutdown(TIMEOUT));
        assertEquals(4000, received.get());
    }

    @Test
    void stressAggregatesThroughTheChain() {
        BookingSystem bookings = new BookingSystem();
        RevenueManager revenue = new RevenueManager(100, Long.MAX_VALUE);
        ManagerDashboard dashboard = new ManagerDashboard();
        bookings.subscribe(BookingEventType.CREATED, revenue::onBooking);
        revenue.subscribe(RevenueEventType.REVENUE_UPDATED, dashboard::onRevenue);

        ProducerPool pool = new ProducerPool(THREADS);
        bookFromAllThreads(pool, bookings);

        assertTrue(pool.shutdown(TIMEOUT));
        assertEquals(40_000, revenue.total());
        assertEquals(40, dashboard.reports());
        assertEquals(40_000, dashboard.latestTotal());
    }

    @Test
    void listenersCanJoinAndLeaveWhileOthersPublish() throws InterruptedException {
        BookingSystem bookings = new BookingSystem();
        AtomicInteger stable = new AtomicInteger();
        bookings.subscribe(BookingEventType.CREATED, event -> stable.incrementAndGet());

        AtomicBoolean running = new AtomicBoolean(true);
        Observer<BookingEvent> passerBy = event -> { };
        Thread churn = new Thread(() -> {
            while (running.get()) {
                bookings.subscribe(BookingEventType.CREATED, passerBy);
                bookings.unsubscribe(BookingEventType.CREATED, passerBy);
            }
        });
        churn.start();

        ProducerPool pool = new ProducerPool(THREADS);
        bookFromAllThreads(pool, bookings);

        assertTrue(pool.shutdown(TIMEOUT));
        running.set(false);
        churn.join();
        assertEquals(4000, stable.get());
    }

    // whatever order the threads finish in, the kept plan must list every dirty room
    @RepeatedTest(20)
    void housekeepingEndsUpWithTheFreshestPlan() {
        List<HotelElement> rooms = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            rooms.add(new Room("r" + i, 1, 50));
        }
        Hotel hotel = new Hotel("Busy", List.of(new Floor(1, rooms)));
        RoomStatusBoard board = new RoomStatusBoard();
        Housekeeping housekeeping = new Housekeeping(board);
        CleaningDispatcher dispatcher = new CleaningDispatcher(hotel, board::statusOf);
        board.subscribe(UnitStatus.DIRTY, dispatcher::onRoomStatus);
        dispatcher.subscribe(CleaningPlanEventType.PLAN_READY, housekeeping::onPlan);

        ProducerPool pool = new ProducerPool(THREADS);
        for (int t = 0; t < THREADS; t++) {
            int offset = t * 10;
            pool.submit(() -> {
                for (int i = offset; i < offset + 10; i++) {
                    board.update(1, "r" + i, UnitStatus.DIRTY);
                }
            });
        }

        assertTrue(pool.shutdown(TIMEOUT));
        assertEquals(40, housekeeping.planFor(1).orElseThrow().tasks().size());
    }

    @Test
    @Timeout(10)
    void stuckProducerIsInterruptedAfterTimeout() throws InterruptedException {
        CountDownLatch never = new CountDownLatch(1);
        CountDownLatch stopped = new CountDownLatch(1);
        ProducerPool pool = new ProducerPool(1);
        pool.submit(() -> {
            try {
                never.await();
            } catch (InterruptedException e) {
                stopped.countDown();
            }
        });

        assertFalse(pool.shutdown(Duration.ofMillis(100)));
        stopped.await();
    }
}
