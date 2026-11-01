package hotel.tree;

import hotel.visitor.Visitor;

public record Suite(String id, int bedrooms, int guests, long pricePerNight, long loungeFee) implements HotelElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSuite(this);
    }
}
