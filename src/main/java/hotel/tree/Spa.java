package hotel.tree;

import hotel.visitor.Visitor;

public record Spa(String id, int treatmentRooms, long dailyRatePerRoom) implements HotelElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSpa(this);
    }
}
