package hotel.integration;

import hotel.observer.Event;
import hotel.visitor.CleaningTask;

import java.util.List;

public record CleaningPlanEvent(CleaningPlanEventType type, int floor, List<CleaningTask> tasks,
                                int totalMinutes, long sequence) implements Event<CleaningPlanEventType> {
    public CleaningPlanEvent {
        tasks = List.copyOf(tasks);
    }
}
