package hotel;

import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Hall;
import hotel.tree.Hotel;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

import java.util.Map;
import java.util.List;
import java.util.function.Function;

// The same small hotel for all visitor tests, so results can be counted by hand.
public final class TestHotel {
    public static final Room ROOM_101 = new Room("101", 2, 80);
    public static final Room ROOM_102 = new Room("102", 1, 50);
    public static final Room ROOM_103 = new Room("103", 1, 50);
    public static final Hall HALL_1 = new Hall("H1", 100, 500);
    public static final Restaurant RESTAURANT = new Restaurant("R1", 40, 15);
    public static final Room ROOM_201 = new Room("201", 3, 100);
    public static final Suite SUITE_21 = new Suite("S21", 2, 4, 300, 40);
    public static final Hall HALL_2 = new Hall("H2", 20, 200);

    public static final Floor FIRST_FLOOR =
            new Floor(1, List.of(ROOM_101, ROOM_102, ROOM_103, HALL_1, RESTAURANT));
    public static final Floor SECOND_FLOOR =
            new Floor(2, List.of(ROOM_201, SUITE_21, HALL_2));
    public static final Hotel HOTEL = new Hotel("Test", List.of(FIRST_FLOOR, SECOND_FLOOR));

    private TestHotel() {
    }

    public static Function<String, UnitStatus> statuses(Map<String, UnitStatus> known) {
        return id -> known.getOrDefault(id, UnitStatus.FREE);
    }
}
