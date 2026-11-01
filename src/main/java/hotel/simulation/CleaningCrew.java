package hotel.simulation;

import hotel.listeners.Housekeeping;
import hotel.rooms.RoomStatusBoard;
import hotel.rooms.UnitStatus;

import java.util.List;

// Producer for the second subject: goes around a floor and cleans what is dirty.
public class CleaningCrew implements Runnable {
    private final RoomStatusBoard board;
    private final Housekeeping housekeeping;
    private final int floor;
    private final List<String> units;
    private final int rounds;

    public CleaningCrew(RoomStatusBoard board, Housekeeping housekeeping, int floor, List<String> units, int rounds) {
        this.board = board;
        this.housekeeping = housekeeping;
        this.floor = floor;
        this.units = List.copyOf(units);
        this.rounds = rounds;
    }

    @Override
    public void run() {
        for (int round = 0; round < rounds; round++) {
            for (String unit : units) {
                if (board.statusOf(unit) == UnitStatus.DIRTY) {
                    housekeeping.finishCleaning(floor, unit);
                }
            }
        }
    }
}
