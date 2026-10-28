package common;

import java.util.ArrayList;
import java.util.List;

public class ElevatorStateDTO {
    private String elevatorId;
    private int currentFloor;
    private Direction direction;
    private DoorState doorState;
    private List<Integer> nextStops;

    public ElevatorStateDTO() {
        this.nextStops = new ArrayList<>();
    }

    public ElevatorStateDTO(String elevatorId, int currentFloor, Direction direction, DoorState doorState,
            List<Integer> nextStops) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.doorState = doorState;
        this.nextStops = (nextStops != null) ? nextStops : new ArrayList<>();
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public DoorState getDoorState() {
        return doorState;
    }

    public void setDoorState(DoorState doorState) {
        this.doorState = doorState;
    }

    public List<Integer> getNextStops() {
        return nextStops;
    }

    public void setNextStops(List<Integer> nextStops) {
        this.nextStops = nextStops;
    }
}
