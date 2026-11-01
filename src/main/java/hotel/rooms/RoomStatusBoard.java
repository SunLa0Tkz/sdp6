package hotel.rooms;

import hotel.observer.EventSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomStatusBoard extends EventSource<UnitStatus, RoomStatusEvent> {
    private final Map<String, UnitStatus> statuses = new ConcurrentHashMap<>();

    public void update(int floor, String unitId, UnitStatus status) {
        UnitStatus previous = statuses.put(unitId, status);
        if (previous != status) {
            publish(new RoomStatusEvent(status, floor, unitId));
        }
    }

    public UnitStatus statusOf(String unitId) {
        return statuses.getOrDefault(unitId, UnitStatus.FREE);
    }
}
