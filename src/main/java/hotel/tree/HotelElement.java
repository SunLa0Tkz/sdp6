package hotel.tree;

import hotel.visitor.Visitor;

public interface HotelElement {
    <R> R accept(Visitor<R> visitor);
}
