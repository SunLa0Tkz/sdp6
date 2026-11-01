package hotel.visitor;

import hotel.tree.Floor;
import hotel.tree.Hall;
import hotel.tree.Hotel;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Suite;

public interface Visitor<R> {
    R visitHotel(Hotel hotel);

    R visitFloor(Floor floor);

    R visitRoom(Room room);

    R visitSuite(Suite suite);

    R visitHall(Hall hall);

    R visitRestaurant(Restaurant restaurant);
}
