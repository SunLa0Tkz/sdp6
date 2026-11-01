package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Spa;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static hotel.TestHotel.ROOM_101;
import static hotel.TestHotel.statuses;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpaTest {
    private final Spa spa = new Spa("SP1", 3, 120);
    private final Floor floor = new Floor(5, List.of(ROOM_101, spa));
    private final Function<String, UnitStatus> dirtySpa = statuses(Map.of("SP1", UnitStatus.DIRTY));

    @Test
    void spaEarnsPerTreatmentRoom() {
        assertEquals(360L, floor.accept(new RevenueVisitor(dirtySpa)));
        assertEquals(0L, floor.accept(new RevenueVisitor(statuses(Map.of("SP1", UnitStatus.OUT_OF_ORDER)))));
    }

    @Test
    void spaIsNotSoldAsARoom() {
        assertEquals(new OccupancyReport(0, 1), floor.accept(new OccupancyVisitor(dirtySpa)));
        assertEquals(List.of(ROOM_101), floor.accept(new FreeUnitsVisitor(dirtySpa, 1)));
        assertEquals(2, floor.accept(new CapacityVisitor()));
    }

    @Test
    void spaGetsCleanedAndExported() {
        CleaningPlanVisitor plan = new CleaningPlanVisitor(dirtySpa);

        assertEquals(60, floor.accept(plan));
        assertEquals(List.of(new CleaningTask("SP1", 0, 60)), plan.tasks());
        assertEquals("{\"type\":\"spa\",\"id\":\"SP1\",\"treatmentRooms\":3,\"dailyRatePerRoom\":120,\"status\":\"DIRTY\"}",
                spa.accept(new JsonExportVisitor(dirtySpa)));
    }
}
