package ui;

public interface FloorUIInterface {
    void initializeAtFloor(int installationFloor);
    char inputDestinationFloor(int destinationFloor);
    void displayAssignedElevator(char elevatorId);
}