package central;

import common.Direction;
import common.DoorState;
import common.ElevatorStateDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Αναπαριστά την τρέχουσα κατάσταση και τα στοιχεία δικτύου ενός ανελκυστήρα στο Central.
 */
public class ElevatorInfo {

    public static final int MAX_PASSENGERS = 8;

    private final String elevatorId;
    private final String baseUrl;
    private int currentFloor;
    private Direction direction;
    private DoorState doorState;
    private int passengerCount;
    private final List<Integer> nextStops;

    public ElevatorInfo(String elevatorId, String baseUrl, int initialFloor) {
        this.elevatorId = elevatorId;
        this.baseUrl = baseUrl;
        this.currentFloor = initialFloor;
        this.direction = Direction.IDLE;
        this.doorState = DoorState.CLOSED;
        this.passengerCount = 0;
        this.nextStops = new ArrayList<>();
    }

    public synchronized void updateFromStateDto(ElevatorStateDTO dto) {
        if (dto == null) {
            return;
        }
        this.currentFloor = dto.getCurrentFloor();
        if (dto.getDirection() != null) {
            this.direction = dto.getDirection();
        }
        if (dto.getDoorState() != null) {
            this.doorState = dto.getDoorState();
        }
        this.nextStops.clear();
        if (dto.getNextStops() != null) {
            this.nextStops.addAll(dto.getNextStops());
        }
    }

    public synchronized void assignTrip(int fromFloor, int toFloor) {
        if (!nextStops.contains(fromFloor) && fromFloor != currentFloor) {
            nextStops.add(fromFloor);
        }
        if (!nextStops.contains(toFloor)) {
            nextStops.add(toFloor);
        }
        if (fromFloor > currentFloor) {
            this.direction = Direction.UP;
        } else if (fromFloor < currentFloor) {
            this.direction = Direction.DOWN;
        } else if (toFloor > currentFloor) {
            this.direction = Direction.UP;
        } else if (toFloor < currentFloor) {
            this.direction = Direction.DOWN;
        }
    }

    public synchronized boolean hasAvailableCapacity() {
        return passengerCount < MAX_PASSENGERS;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized void setDirection(Direction direction) {
        this.direction = direction;
    }

    public synchronized DoorState getDoorState() {
        return doorState;
    }

    public synchronized int getPassengerCount() {
        return passengerCount;
    }

    public synchronized void setPassengerCount(int passengerCount) {
        this.passengerCount = Math.max(0, passengerCount);
    }

    public synchronized List<Integer> getNextStops() {
        return Collections.unmodifiableList(new ArrayList<>(nextStops));
    }
}
