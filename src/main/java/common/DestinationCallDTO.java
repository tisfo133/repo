package common;

public class DestinationCallDTO {
    private int fromFloor;
    private int toFloor;

    public DestinationCallDTO() {
    }

    public DestinationCallDTO(int fromFloor, int toFloor) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public void setToFloor(int toFloor) {
        this.toFloor = toFloor;
    }

    /**
     * Calculates movement direction based on target and current floor.
     *
     * @return Direction.UP, Direction.DOWN or Direction.IDLE
     */
    public Direction getDirection() {
        if (toFloor > fromFloor) {
            return Direction.UP;
        } else if (toFloor < fromFloor) {
            return Direction.DOWN;
        }
        return Direction.IDLE;
    }
}
