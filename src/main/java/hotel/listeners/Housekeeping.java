package hotel.listeners;

import hotel.integration.CleaningPlanEvent;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.UnitStatus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Housekeeping {
    private final RoomStatusBoard board;
    private final Map<Integer, CleaningPlanEvent> plans = new ConcurrentHashMap<>();

    public Housekeeping(RoomStatusBoard board) {
        this.board = board;
    }

    public void onPlan(CleaningPlanEvent plan) {
        // plans for one floor can arrive out of order, the higher sequence is the fresher one
        plans.merge(plan.floor(), plan, (old, fresh) -> fresh.sequence() > old.sequence() ? fresh : old);
    }

    public void finishCleaning(int floor, String unitId) {
        board.update(floor, unitId, UnitStatus.FREE);
    }

    public Optional<CleaningPlanEvent> planFor(int floor) {
        return Optional.ofNullable(plans.get(floor));
    }
}
