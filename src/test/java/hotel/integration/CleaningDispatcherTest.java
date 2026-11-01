package hotel.integration;

import hotel.TestHotel;
import hotel.listeners.Housekeeping;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.UnitStatus;
import hotel.visitor.CleaningTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleaningDispatcherTest {
    private final RoomStatusBoard board = new RoomStatusBoard();
    private final Housekeeping housekeeping = new Housekeeping(board);
    private final CleaningDispatcher dispatcher = new CleaningDispatcher(TestHotel.HOTEL, board::statusOf);

    @BeforeEach
    void wire() {
        board.subscribe(UnitStatus.DIRTY, dispatcher::onRoomStatus);
        board.subscribe(UnitStatus.FREE, dispatcher::onRoomStatus);
        for (CleaningPlanEventType type : CleaningPlanEventType.values()) {
            dispatcher.subscribe(type, housekeeping::onPlan);
        }
    }

    @Test
    void dirtyRoomProducesPlanForItsFloorOnly() {
        board.update(2, "S21", UnitStatus.DIRTY);
        board.update(1, "102", UnitStatus.DIRTY);

        CleaningPlanEvent plan = housekeeping.planFor(1).orElseThrow();

        assertEquals(CleaningPlanEventType.PLAN_READY, plan.type());
        assertEquals(List.of(new CleaningTask("102", 0, 25)), plan.tasks());
        assertEquals(25, plan.totalMinutes());
    }

    @Test
    void floorWithoutEventsGetsNoPlan() {
        board.update(1, "102", UnitStatus.DIRTY);

        assertTrue(housekeeping.planFor(2).isEmpty());
    }

    @Test
    void cleaningTheLastRoomReportsCleanFloor() {
        board.update(1, "102", UnitStatus.DIRTY);

        housekeeping.finishCleaning(1, "102");

        CleaningPlanEvent plan = housekeeping.planFor(1).orElseThrow();
        assertEquals(CleaningPlanEventType.FLOOR_CLEAN, plan.type());
        assertEquals(0, plan.totalMinutes());
    }

    @Test
    void olderPlanDoesNotReplaceFresherOne() {
        CleaningPlanEvent fresh = new CleaningPlanEvent(CleaningPlanEventType.PLAN_READY, 1, List.of(), 50, 2);
        CleaningPlanEvent old = new CleaningPlanEvent(CleaningPlanEventType.PLAN_READY, 1, List.of(), 25, 1);

        housekeeping.onPlan(fresh);
        housekeeping.onPlan(old);

        assertEquals(fresh, housekeeping.planFor(1).orElseThrow());
    }
}
