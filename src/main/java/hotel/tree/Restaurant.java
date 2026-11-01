package hotel.tree;

import hotel.visitor.Visitor;

public record Restaurant(String id, int seats, long averageCheck) implements HotelElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitRestaurant(this);
    }
}
