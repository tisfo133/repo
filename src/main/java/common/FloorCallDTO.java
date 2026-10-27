package common;

public class FloorCallDTO {
    private int floor;
    private Direction direction;

    public FloorCallDTO() {
    }

    public FloorCallDTO(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
