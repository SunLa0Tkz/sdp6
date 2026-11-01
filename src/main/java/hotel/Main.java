package hotel;

import hotel.booking.Booking;
import hotel.booking.BookingEventType;
import hotel.booking.BookingSystem;
import hotel.integration.CleaningDispatcher;
import hotel.integration.CleaningPlanEventType;
import hotel.listeners.FrontDesk;
import hotel.listeners.GuestApp;
import hotel.listeners.Housekeeping;
import hotel.listeners.ManagerDashboard;
import hotel.listeners.RevenueEventType;
import hotel.listeners.RevenueManager;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.UnitStatus;
import hotel.simulation.BookingTraffic;
import hotel.simulation.CleaningCrew;
import hotel.simulation.ProducerPool;
import hotel.tree.Floor;
import hotel.tree.Hall;
import hotel.tree.Hotel;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;
import hotel.visitor.FreeUnitsVisitor;
import hotel.visitor.JsonExportVisitor;
import hotel.visitor.OccupancyReport;
import hotel.visitor.OccupancyVisitor;
import hotel.visitor.RevenueVisitor;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

// Composition root. Usage: Main [producer threads] [bookings per producer]
public class Main {
    private static final List<String> FIRST_FLOOR_UNITS = List.of("101", "102", "103", "H1");
    private static final List<String> SECOND_FLOOR_UNITS = List.of("201", "202", "S21");

    public static void main(String[] args) {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int bookingsPerProducer = args.length > 1 ? Integer.parseInt(args[1]) : 200;

        Hotel hotel = buildHotel();

        BookingSystem bookings = new BookingSystem();
        RoomStatusBoard board = new RoomStatusBoard();
        Function<String, UnitStatus> statuses = board::statusOf;

        FrontDesk frontDesk = new FrontDesk(board);
        Housekeeping housekeeping = new Housekeeping(board);
        RevenueManager revenue = new RevenueManager(100, 50_000);
        ManagerDashboard dashboard = new ManagerDashboard();
        CleaningDispatcher dispatcher = new CleaningDispatcher(hotel, statuses);
        GuestApp guestApp = new GuestApp(bookings, "vip");

        for (BookingEventType type : BookingEventType.values()) {
            bookings.subscribe(type, frontDesk::onBooking);
        }
        bookings.subscribe(BookingEventType.CREATED, revenue::onBooking);
        bookings.subscribe(BookingEventType.CANCELLED, revenue::onBooking);
        for (UnitStatus status : UnitStatus.values()) {
            board.subscribe(status, frontDesk::onRoomStatus);
        }
        board.subscribe(UnitStatus.DIRTY, dispatcher::onRoomStatus);
        board.subscribe(UnitStatus.FREE, dispatcher::onRoomStatus);
        for (CleaningPlanEventType type : CleaningPlanEventType.values()) {
            dispatcher.subscribe(type, housekeeping::onPlan);
        }
        for (RevenueEventType type : RevenueEventType.values()) {
            revenue.subscribe(type, dashboard::onRevenue);
        }
        guestApp.connect();

        bookings.create(new Booking("vip", "Anna Petrova", 3, "S32", 900));
        bookings.checkIn("vip");

        ProducerPool pool = new ProducerPool(threads);
        for (int i = 0; i < threads; i++) {
            int floor = i % 2 + 1;
            List<String> units = floor == 1 ? FIRST_FLOOR_UNITS : SECOND_FLOOR_UNITS;
            pool.submit(new BookingTraffic(bookings, "t" + i, floor, units, bookingsPerProducer));
        }
        pool.submit(new CleaningCrew(board, housekeeping, 1, FIRST_FLOOR_UNITS, bookingsPerProducer));
        boolean finishedInTime = pool.shutdown(Duration.ofSeconds(30));

        bookings.checkOut("vip");

        OccupancyReport occupancy = hotel.accept(new OccupancyVisitor(statuses));
        System.out.println("Producers finished in time: " + finishedInTime);
        System.out.println("Booked revenue: " + revenue.total() + ", reports: " + dashboard.reports()
                + ", target reached: " + dashboard.targetReached());
        System.out.println("Guests in house: " + frontDesk.guestsInHouse());
        System.out.println("Guest app: " + guestApp.messages());
        System.out.println("Income today: " + hotel.accept(new RevenueVisitor(statuses)));
        System.out.printf("Occupancy: %d of %d (%.1f%%)%n",
                occupancy.occupied(), occupancy.total(), occupancy.percent());
        System.out.println("Free for 2 guests: " + hotel.accept(new FreeUnitsVisitor(statuses, 2)));
        for (Floor floor : hotel.floors()) {
            housekeeping.planFor(floor.number()).ifPresent(plan -> System.out.println(
                    "Floor " + plan.floor() + " cleaning: " + plan.totalMinutes() + " min " + plan.tasks()));
        }
        System.out.println(hotel.accept(new JsonExportVisitor(statuses)));
    }

    private static Hotel buildHotel() {
        return new Hotel("Grand Almaty", List.of(
                new Floor(1, List.of(
                        new Room("101", 2, 80),
                        new Room("102", 1, 50),
                        new Room("103", 2, 80),
                        new Hall("H1", 100, 500),
                        new Restaurant("R1", 40, 15))),
                new Floor(2, List.of(
                        new Room("201", 2, 90),
                        new Room("202", 3, 110),
                        new Suite("S21", 2, 4, 300, 40))),
                new Floor(3, List.of(
                        new Room("301", 2, 90),
                        new Suite("S31", 2, 4, 320, 40),
                        new Suite("S32", 3, 6, 450, 60)))));
    }
}
