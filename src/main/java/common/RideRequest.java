package common;

/**
 * Αντιπροσωπεύει ένα αίτημα επιβάτη από κάποιον όροφο προς έναν προορισμό.
 */
public class RideRequest {
    private int sourceFloor;
    private int destinationFloor;

    public RideRequest(int sourceFloor, int destinationFloor) {
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }
}
