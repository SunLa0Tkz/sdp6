package hotel.tree;

import hotel.visitor.Visitor;

public record Hall(String id, int seats, long dayRate) implements HotelElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitHall(this);
    }
}
