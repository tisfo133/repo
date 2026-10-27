package elevator;

import java.util.List;

public record ElevatorStatusDto(
        char elevatorId,
        int currentFloor,
        ElevatorState state,
        DoorState doorState,
        List<Integer> targetFloors
) {
}