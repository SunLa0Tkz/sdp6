package hotel.rooms;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomStatusBoardTest {
    private final RoomStatusBoard board = new RoomStatusBoard();

    @Test
    void unknownUnitIsFree() {
        assertEquals(UnitStatus.FREE, board.statusOf("101"));
    }

    @Test
    void publishesOnlyRealChanges() {
        List<RoomStatusEvent> events = new ArrayList<>();
        board.subscribe(UnitStatus.DIRTY, events::add);

        board.update(1, "101", UnitStatus.DIRTY);
        board.update(1, "101", UnitStatus.DIRTY);

        assertEquals(List.of(new RoomStatusEvent(UnitStatus.DIRTY, 1, "101")), events);
    }
}
