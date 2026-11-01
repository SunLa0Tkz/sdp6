package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Hall;
import hotel.tree.Hotel;
import hotel.tree.HotelElement;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Spa;
import hotel.tree.Suite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Keeps a running clock across the tree: each task starts when the previous one ends.
// Every visit returns the minutes planned so far. Has state, so use one instance per run.
public class CleaningPlanVisitor implements Visitor<Integer> {
    private final Function<String, UnitStatus> statuses;
    private final List<CleaningTask> tasks = new ArrayList<>();
    private int clock;

    public CleaningPlanVisitor(Function<String, UnitStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    public Integer visitHotel(Hotel hotel) {
        hotel.floors().forEach(floor -> floor.accept(this));
        return clock;
    }

    @Override
    public Integer visitFloor(Floor floor) {
        for (HotelElement unit : floor.units()) {
            unit.accept(this);
        }
        return clock;
    }

    @Override
    public Integer visitRoom(Room room) {
        return planIfDirty(room.id(), 20 + 5 * room.beds());
    }

    @Override
    public Integer visitSuite(Suite suite) {
        return planIfDirty(suite.id(), 15 + 30 * suite.bedrooms());
    }

    @Override
    public Integer visitHall(Hall hall) {
        return planIfDirty(hall.id(), 30 + hall.seats() / 5);
    }

    @Override
    public Integer visitRestaurant(Restaurant restaurant) {
        return planIfDirty(restaurant.id(), 45 + restaurant.seats() / 2);
    }

    @Override
    public Integer visitSpa(Spa spa) {
        return planIfDirty(spa.id(), 20 * spa.treatmentRooms());
    }

    private int planIfDirty(String unitId, int minutes) {
        if (statuses.apply(unitId) == UnitStatus.DIRTY) {
            tasks.add(new CleaningTask(unitId, clock, minutes));
            clock += minutes;
        }
        return clock;
    }

    public List<CleaningTask> tasks() {
        return List.copyOf(tasks);
    }
}
