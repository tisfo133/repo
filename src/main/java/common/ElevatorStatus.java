package common;

/**
 * Καταγράφει την τρέχουσα κατάσταση ενός θαλάμου ανελκυστήρα (θέση, κατεύθυνση κ.λπ.).
 */
public class ElevatorStatus {
    private char elevatorId;
    private int currentFloor;
    private boolean movingUp;
    private boolean doorsOpen;

    public ElevatorStatus(char elevatorId, int currentFloor, boolean movingUp, boolean doorsOpen) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.movingUp = movingUp;
        this.doorsOpen = doorsOpen;
    }

    public char getElevatorId() {
        return elevatorId;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public boolean isMovingUp() {
        return movingUp;
    }

    public boolean areDoorsOpen() {
        return doorsOpen;
    }
}
