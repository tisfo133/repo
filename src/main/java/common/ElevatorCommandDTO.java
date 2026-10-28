package common;

public class ElevatorCommandDTO {
    private String elevatorId;
    private int targetFloor;

    public ElevatorCommandDTO() {
    }

    public ElevatorCommandDTO(String elevatorId, int targetFloor) {
        this.elevatorId = elevatorId;
        this.targetFloor = targetFloor;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public void setTargetFloor(int targetFloor) {
        this.targetFloor = targetFloor;
    }
}
