package hotel.rooms;

import hotel.observer.Event;

public record RoomStatusEvent(UnitStatus type, int floor, String unitId) implements Event<UnitStatus> {
}
