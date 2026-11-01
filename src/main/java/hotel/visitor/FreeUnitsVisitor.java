package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Hall;
import hotel.tree.HotelElement;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Filter: units that can be sold right now to a group of the given size.
public class FreeUnitsVisitor extends TreeFold<List<HotelElement>> {
    private final Function<String, UnitStatus> statuses;
    private final int groupSize;

    public FreeUnitsVisitor(Function<String, UnitStatus> statuses, int groupSize) {
        this.statuses = statuses;
        this.groupSize = groupSize;
    }

    @Override
    protected List<HotelElement> empty() {
        return List.of();
    }

    @Override
    protected List<HotelElement> combine(List<HotelElement> left, List<HotelElement> right) {
        List<HotelElement> all = new ArrayList<>(left);
        all.addAll(right);
        return all;
    }

    @Override
    public List<HotelElement> visitRoom(Room room) {
        return keepIfFree(room, room.id(), room.beds());
    }

    @Override
    public List<HotelElement> visitSuite(Suite suite) {
        return keepIfFree(suite, suite.id(), suite.guests());
    }

    @Override
    public List<HotelElement> visitHall(Hall hall) {
        return keepIfFree(hall, hall.id(), hall.seats());
    }

    @Override
    public List<HotelElement> visitRestaurant(Restaurant restaurant) {
        return List.of();
    }

    private List<HotelElement> keepIfFree(HotelElement unit, String unitId, int capacity) {
        if (statuses.apply(unitId) == UnitStatus.FREE && capacity >= groupSize) {
            return List.of(unit);
        }
        return List.of();
    }
}
