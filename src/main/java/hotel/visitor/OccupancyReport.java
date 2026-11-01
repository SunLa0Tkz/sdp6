package hotel.visitor;

public record OccupancyReport(int occupied, int total) {
    public OccupancyReport plus(OccupancyReport other) {
        return new OccupancyReport(occupied + other.occupied, total + other.total);
    }

    public double percent() {
        return total == 0 ? 0 : 100.0 * occupied / total;
    }
}
