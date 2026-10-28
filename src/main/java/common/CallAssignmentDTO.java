package common;

public class CallAssignmentDTO {
    private String elevatorId;
    private int destinationFloor;

    public CallAssignmentDTO() {
    }

    public CallAssignmentDTO(String elevatorId, int destinationFloor) {
        this.elevatorId = elevatorId;
        this.destinationFloor = destinationFloor;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }
}
