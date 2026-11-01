package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Hall;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

import java.util.function.Function;

// Expected income for today, in whole currency units.
public class RevenueVisitor extends TreeFold<Long> {
    private final Function<String, UnitStatus> statuses;

    public RevenueVisitor(Function<String, UnitStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    protected Long empty() {
        return 0L;
    }

    @Override
    protected Long combine(Long left, Long right) {
        return left + right;
    }

    @Override
    public Long visitRoom(Room room) {
        return ifOccupied(room.id(), room.pricePerNight());
    }

    @Override
    public Long visitSuite(Suite suite) {
        return ifOccupied(suite.id(), suite.pricePerNight() + suite.loungeFee());
    }

    @Override
    public Long visitHall(Hall hall) {
        return ifOccupied(hall.id(), hall.dayRate());
    }

    // the restaurant earns without bookings, unless it is closed
    @Override
    public Long visitRestaurant(Restaurant restaurant) {
        if (statuses.apply(restaurant.id()) == UnitStatus.OUT_OF_ORDER) {
            return 0L;
        }
        return restaurant.seats() * restaurant.averageCheck();
    }

    private long ifOccupied(String unitId, long income) {
        return statuses.apply(unitId) == UnitStatus.OCCUPIED ? income : 0L;
    }
}
