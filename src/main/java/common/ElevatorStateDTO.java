package common;

public class ElevatorStateDTO {
    private int elevatorId;
    private int currentFloor;
    private Direction direction;
    private DoorState doorState;

    public ElevatorStateDTO() {
    }

    public ElevatorStateDTO(int elevatorId, int currentFloor, Direction direction, DoorState doorState) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.doorState = doorState;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(int elevatorId) {
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
}
