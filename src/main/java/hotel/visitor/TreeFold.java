package hotel.visitor;

import hotel.tree.Floor;
import hotel.tree.Hotel;
import hotel.tree.HotelElement;

import java.util.List;

// For visitors whose result is just the leaf results put together.
abstract class TreeFold<R> implements Visitor<R> {
    protected abstract R empty();

    protected abstract R combine(R left, R right);

    @Override
    public R visitHotel(Hotel hotel) {
        return foldAll(hotel.floors());
    }

    @Override
    public R visitFloor(Floor floor) {
        return foldAll(floor.units());
    }

    private R foldAll(List<? extends HotelElement> children) {
        R result = empty();
        for (HotelElement child : children) {
            result = combine(result, child.accept(this));
        }
        return result;
    }
}
