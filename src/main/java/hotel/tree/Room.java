package hotel.tree;

import hotel.visitor.Visitor;

public record Room(String id, int beds, long pricePerNight) implements HotelElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitRoom(this);
    }
}
