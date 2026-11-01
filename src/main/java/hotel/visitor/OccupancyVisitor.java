package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Hall;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

import java.util.function.Function;

public class OccupancyVisitor extends TreeFold<OccupancyReport> {
    private static final OccupancyReport NOT_COUNTED = new OccupancyReport(0, 0);

    private final Function<String, UnitStatus> statuses;

    public OccupancyVisitor(Function<String, UnitStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    protected OccupancyReport empty() {
        return NOT_COUNTED;
    }

    @Override
    protected OccupancyReport combine(OccupancyReport left, OccupancyReport right) {
        return left.plus(right);
    }

    @Override
    public OccupancyReport visitRoom(Room room) {
        return reportFor(room.id());
    }

    @Override
    public OccupancyReport visitSuite(Suite suite) {
        return reportFor(suite.id());
    }

    @Override
    public OccupancyReport visitHall(Hall hall) {
        return reportFor(hall.id());
    }

    @Override
    public OccupancyReport visitRestaurant(Restaurant restaurant) {
        return NOT_COUNTED;
    }

    // a unit out of order can't be sold, so it doesn't count against occupancy
    private OccupancyReport reportFor(String unitId) {
        return switch (statuses.apply(unitId)) {
            case OCCUPIED -> new OccupancyReport(1, 1);
            case OUT_OF_ORDER -> NOT_COUNTED;
            default -> new OccupancyReport(0, 1);
        };
    }
}
