package hotel.visitor;

import hotel.tree.Hall;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

// How many guests can sleep in the hotel. Halls and the restaurant don't count.
public class CapacityVisitor extends TreeFold<Integer> {
    @Override
    protected Integer empty() {
        return 0;
    }

    @Override
    protected Integer combine(Integer left, Integer right) {
        return left + right;
    }

    @Override
    public Integer visitRoom(Room room) {
        return room.beds();
    }

    @Override
    public Integer visitSuite(Suite suite) {
        return suite.guests();
    }

    @Override
    public Integer visitHall(Hall hall) {
        return 0;
    }

    @Override
    public Integer visitRestaurant(Restaurant restaurant) {
        return 0;
    }
}
