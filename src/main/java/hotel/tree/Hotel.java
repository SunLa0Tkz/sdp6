package hotel.tree;

import hotel.visitor.Visitor;

import java.util.List;

public record Hotel(String name, List<Floor> floors) implements HotelElement {
    public Hotel {
        floors = List.copyOf(floors);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitHotel(this);
    }
}
