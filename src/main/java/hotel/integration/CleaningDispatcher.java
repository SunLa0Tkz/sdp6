package hotel.integration;

import hotel.observer.EventSource;
import hotel.rooms.RoomStatusEvent;
import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Hotel;
import hotel.visitor.CleaningPlanVisitor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

// The link between the two patterns: a room status event runs the cleaning visitor
// on the floor of that room only, the plan goes out as a new event.
public class CleaningDispatcher extends EventSource<CleaningPlanEventType, CleaningPlanEvent> {
    private final Map<Integer, Floor> floors;
    private final Function<String, UnitStatus> statuses;
    private final AtomicLong sequence = new AtomicLong();

    public CleaningDispatcher(Hotel hotel, Function<String, UnitStatus> statuses) {
        this.floors = hotel.floors().stream().collect(Collectors.toUnmodifiableMap(Floor::number, f -> f));
        this.statuses = statuses;
    }

    public void onRoomStatus(RoomStatusEvent event) {
        Floor floor = floors.get(event.floor());
        if (floor == null) {
            throw new IllegalArgumentException("No such floor: " + event.floor());
        }
        // taken before reading statuses, so a higher number always means a fresher plan
        long number = sequence.incrementAndGet();

        CleaningPlanVisitor visitor = new CleaningPlanVisitor(statuses);
        int minutes = floor.accept(visitor);
        CleaningPlanEventType type = minutes == 0 ? CleaningPlanEventType.FLOOR_CLEAN : CleaningPlanEventType.PLAN_READY;
        publish(new CleaningPlanEvent(type, event.floor(), visitor.tasks(), minutes, number));
    }
}
