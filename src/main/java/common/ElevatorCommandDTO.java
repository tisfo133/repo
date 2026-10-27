package common;

public class ElevatorCommandDTO {
    private int elevatorId;
    private int targetFloor;

    public ElevatorCommandDTO() {
    }

    public ElevatorCommandDTO(int elevatorId, int targetFloor) {
        this.elevatorId = elevatorId;
        this.targetFloor = targetFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public void setTargetFloor(int targetFloor) {
        this.targetFloor = targetFloor;
    }
}
