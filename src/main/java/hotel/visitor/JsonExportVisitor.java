package hotel.visitor;

import hotel.rooms.UnitStatus;
import hotel.tree.Floor;
import hotel.tree.Hall;
import hotel.tree.Hotel;
import hotel.tree.HotelElement;
import hotel.tree.Restaurant;
import hotel.tree.Room;
import hotel.tree.Spa;
import hotel.tree.Suite;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonExportVisitor implements Visitor<String> {
    private final Function<String, UnitStatus> statuses;

    public JsonExportVisitor(Function<String, UnitStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    public String visitHotel(Hotel hotel) {
        return "{\"hotel\":" + quote(hotel.name()) + ",\"floors\":" + array(hotel.floors()) + "}";
    }

    @Override
    public String visitFloor(Floor floor) {
        return "{\"floor\":" + floor.number() + ",\"units\":" + array(floor.units()) + "}";
    }

    @Override
    public String visitRoom(Room room) {
        return unit("room", room.id(), "\"beds\":" + room.beds() + ",\"price\":" + room.pricePerNight());
    }

    @Override
    public String visitSuite(Suite suite) {
        return unit("suite", suite.id(), "\"bedrooms\":" + suite.bedrooms() + ",\"guests\":" + suite.guests()
                + ",\"price\":" + suite.pricePerNight() + ",\"loungeFee\":" + suite.loungeFee());
    }

    @Override
    public String visitHall(Hall hall) {
        return unit("hall", hall.id(), "\"seats\":" + hall.seats() + ",\"dayRate\":" + hall.dayRate());
    }

    @Override
    public String visitRestaurant(Restaurant restaurant) {
        return unit("restaurant", restaurant.id(),
                "\"seats\":" + restaurant.seats() + ",\"averageCheck\":" + restaurant.averageCheck());
    }

    @Override
    public String visitSpa(Spa spa) {
        return unit("spa", spa.id(),
                "\"treatmentRooms\":" + spa.treatmentRooms() + ",\"dailyRatePerRoom\":" + spa.dailyRatePerRoom());
    }

    private String unit(String type, String id, String fields) {
        return "{\"type\":" + quote(type) + ",\"id\":" + quote(id) + "," + fields
                + ",\"status\":" + quote(statuses.apply(id).name()) + "}";
    }

    private String array(List<? extends HotelElement> children) {
        return children.stream()
                .map(child -> child.accept(this))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String quote(String text) {
        StringBuilder json = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"').toString();
    }
}
