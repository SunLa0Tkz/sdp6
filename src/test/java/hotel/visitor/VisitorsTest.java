package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.HotelElement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static hotel.TestHotel.FIRST_FLOOR;
import static hotel.TestHotel.HOTEL;
import static hotel.TestHotel.ROOM_103;
import static hotel.TestHotel.ROOM_201;
import static hotel.TestHotel.SECOND_FLOOR;
import static hotel.TestHotel.statuses;
import static org.junit.jupiter.api.Assertions.assertEquals;

// All expected numbers below are counted by hand from TestHotel.
class VisitorsTest {
    private final Function<String, UnitStatus> busyDay = statuses(Map.of(
            "101", UnitStatus.OCCUPIED,
            "102", UnitStatus.DIRTY,
            "H1", UnitStatus.OCCUPIED,
            "S21", UnitStatus.OCCUPIED,
            "H2", UnitStatus.OUT_OF_ORDER));

    @Test
    void revenueOfWholeHotel() {
        // floor 1: 80 + 500 + 40 * 15 = 1180, floor 2: 300 + 40 = 340
        assertEquals(1520L, HOTEL.accept(new RevenueVisitor(busyDay)));
    }

    @Test
    void revenueOfOneFloor() {
        assertEquals(340L, SECOND_FLOOR.accept(new RevenueVisitor(busyDay)));
    }

    @Test
    void closedRestaurantEarnsNothing() {
        Function<String, UnitStatus> closed = statuses(Map.of("R1", UnitStatus.OUT_OF_ORDER));

        assertEquals(0L, FIRST_FLOOR.accept(new RevenueVisitor(closed)));
    }

    @Test
    void occupancySkipsRestaurantAndBrokenUnits() {
        // counted: 101, 102, 103, H1, 201, S21. occupied: 101, H1, S21. H2 is out of order
        OccupancyReport report = HOTEL.accept(new OccupancyVisitor(busyDay));

        assertEquals(new OccupancyReport(3, 6), report);
        assertEquals(50.0, report.percent());
    }

    @Test
    void occupancyOfEmptyReportIsZero() {
        assertEquals(0.0, new OccupancyReport(0, 0).percent());
    }

    @Test
    void filterFindsFreeUnitsBigEnough() {
        List<HotelElement> anySize = HOTEL.accept(new FreeUnitsVisitor(busyDay, 1));
        List<HotelElement> pair = HOTEL.accept(new FreeUnitsVisitor(busyDay, 2));

        assertEquals(List.of(ROOM_103, ROOM_201), anySize);
        assertEquals(List.of(ROOM_201), pair);
    }

    @Test
    void cleaningPlanKeepsARunningClockAcrossFloors() {
        Function<String, UnitStatus> afterParty = statuses(Map.of(
                "102", UnitStatus.DIRTY,
                "R1", UnitStatus.DIRTY,
                "S21", UnitStatus.DIRTY,
                "H2", UnitStatus.DIRTY));
        CleaningPlanVisitor visitor = new CleaningPlanVisitor(afterParty);

        int total = HOTEL.accept(visitor);

        // 102: 20 + 5 = 25, R1: 45 + 20 = 65, S21: 15 + 60 = 75, H2: 30 + 4 = 34
        assertEquals(199, total);
        assertEquals(List.of(
                new CleaningTask("102", 0, 25),
                new CleaningTask("R1", 25, 65),
                new CleaningTask("S21", 90, 75),
                new CleaningTask("H2", 165, 34)), visitor.tasks());
    }

    @Test
    void jsonKeepsTheNesting() {
        String expected = "{\"floor\":2,\"units\":["
                + "{\"type\":\"room\",\"id\":\"201\",\"beds\":3,\"price\":100,\"status\":\"FREE\"},"
                + "{\"type\":\"suite\",\"id\":\"S21\",\"bedrooms\":2,\"guests\":4,\"price\":300,\"loungeFee\":40,\"status\":\"OCCUPIED\"},"
                + "{\"type\":\"hall\",\"id\":\"H2\",\"seats\":20,\"dayRate\":200,\"status\":\"OUT_OF_ORDER\"}]}";

        assertEquals(expected, SECOND_FLOOR.accept(new JsonExportVisitor(busyDay)));
    }

    @Test
    void jsonEscapesTheHotelName() {
        var hotel = new hotel.tree.Hotel("The \"Grand\"\\Inn", List.of());

        assertEquals("{\"hotel\":\"The \\\"Grand\\\"\\\\Inn\",\"floors\":[]}",
                hotel.accept(new JsonExportVisitor(busyDay)));
    }
}
