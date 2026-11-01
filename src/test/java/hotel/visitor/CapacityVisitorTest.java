package hotel.visitor;

import hotel.TestHotel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapacityVisitorTest {
    @Test
    void countsOnlyPlacesToSleep() {
        // floor 1: 2 + 1 + 1, floor 2: 3 + 4
        assertEquals(11, TestHotel.HOTEL.accept(new CapacityVisitor()));
    }

    @Test
    void worksOnASubtree() {
        assertEquals(7, TestHotel.SECOND_FLOOR.accept(new CapacityVisitor()));
    }
}
