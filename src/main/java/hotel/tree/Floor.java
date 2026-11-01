package hotel.tree;

import hotel.visitor.Visitor;

import java.util.List;

public record Floor(int number, List<HotelElement> units) implements HotelElement {
    public Floor {
        units = List.copyOf(units);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFloor(this);
    }
}
