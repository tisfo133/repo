package elevator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one elevator in the Smart Elevator System.
 *
 * The elevator operates in a building with:
 * - 150 floors
 * - Maximum capacity of 8 passengers
 *
 * Main responsibilities:
 * - Maintain its current state
 * - Manage doors
 * - Manage passengers
 * - Move between floors
 * - Manage requested destinations
 * - Enforce basic safety rules
 */
public class Elevator {

    // =========================================================
    // CONSTANTS
    // =========================================================

    public static final int MIN_FLOOR = 1;
    public static final int MAX_FLOOR = 150;
    public static final int MAX_CAPACITY = 8;


    // =========================================================
    // ENUMS
    // =========================================================

    public enum Direction {
        MOVING_UP,
        MOVING_DOWN,
        IDLE
    }

    public enum DoorState {
        OPEN,
        CLOSED
    }

    public enum ElevatorState {
        IDLE,
        MOVING
    }


    // =========================================================
    // FIELDS
    // =========================================================

    private final int id;

    private int currentFloor;

    private int passengerCount;

    private Direction direction;

    private DoorState doorState;

    private ElevatorState elevatorState;

    private final List<Integer> destinations;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    /**
     * Creates a new elevator.
     *
     * @param id the unique identifier of the elevator
     * @param initialFloor the floor where the elevator starts
     */
    public Elevator(int id, int initialFloor) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Elevator ID must be greater than 0."
            );
        }

        if (!isValidFloor(initialFloor)) {
            throw new IllegalArgumentException(
                    "Initial floor must be between "
                    + MIN_FLOOR + " and " + MAX_FLOOR + "."
            );
        }

        this.id = id;
        this.currentFloor = initialFloor;
        this.passengerCount = 0;

        this.direction = Direction.IDLE;

        this.doorState = DoorState.CLOSED;

        this.elevatorState = ElevatorState.IDLE;

        this.destinations = new ArrayList<>();
    }


    // =========================================================
    // IDENTITY
    // =========================================================

    /**
     * Returns the elevator's unique ID.
     */
    public int getId() {
        return id;
    }


    // =========================================================
    // STATE
    // =========================================================

    /**
     * Returns the current floor of the elevator.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * Returns the number of passengers currently inside.
     */
    public int getPassengerCount() {
        return passengerCount;
    }

    /**
     * Returns the current direction.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Returns the current door state.
     */
    public DoorState getDoorState() {
        return doorState;
    }

    /**
     * Returns the current elevator state.
     */
    public ElevatorState getElevatorState() {
        return elevatorState;
    }


    // =========================================================
    // STATE CHECKS
    // =========================================================

    /**
     * Returns true if the elevator is currently moving.
     */
    public boolean isMoving() {
        return elevatorState == ElevatorState.MOVING;
    }

    /**
     * Returns true if the elevator is idle.
     */
    public boolean isIdle() {
        return elevatorState == ElevatorState.IDLE;
    }

    /**
     * Returns true if the elevator is full.
     */
    public boolean isFull() {
        return passengerCount >= MAX_CAPACITY;
    }

    /**
     * Returns true if the elevator has no passengers.
     */
    public boolean isEmpty() {
        return passengerCount == 0;
    }

    /**
     * Returns true if the doors are open.
     */
    public boolean areDoorsOpen() {
        return doorState == DoorState.OPEN;
    }

    /**
     * Returns true if the doors are closed.
     */
    public boolean areDoorsClosed() {
        return doorState == DoorState.CLOSED;
    }

    /**
     * Returns the number of available passenger spaces.
     */
    public int getAvailableCapacity() {
        return MAX_CAPACITY - passengerCount;
    }


    // =========================================================
    // DOOR OPERATIONS
    // =========================================================

    /**
     * Opens the elevator doors.
     *
     * Doors cannot open while the elevator is moving.
     *
     * @return true if the operation was successful,
     *         false otherwise
     */
    public boolean openDoors() {

        if (isMoving()) {
            return false;
        }

        doorState = DoorState.OPEN;

        return true;
    }

    /**
     * Closes the elevator doors.
     *
     * @return true if the operation was successful
     */
    public boolean closeDoors() {

        doorState = DoorState.CLOSED;

        return true;
    }


    // =========================================================
    // PASSENGER OPERATIONS
    // =========================================================

    /**
     * Adds one passenger to the elevator.
     *
     * A passenger can enter only when:
     * - the doors are open
     * - the elevator is not full
     *
     * @return true if the passenger was added,
     *         false otherwise
     */
    public boolean addPassenger() {

        if (!areDoorsOpen()) {
            return false;
        }

        if (isFull()) {
            return false;
        }

        passengerCount++;

        return true;
    }

    /**
     * Removes one passenger from the elevator.
     *
     * A passenger can exit only when:
     * - the doors are open
     * - there is at least one passenger
     *
     * @return true if the passenger was removed,
     *         false otherwise
     */
    public boolean removePassenger() {

        if (!areDoorsOpen()) {
            return false;
        }

        if (isEmpty()) {
            return false;
        }

        passengerCount--;

        return true;
    }


    // =========================================================
    // MOVEMENT
    // =========================================================

    /**
     * Moves the elevator to the requested floor.
     *
     * Safety rule:
     * The elevator cannot move while its doors are open.
     *
     * The current implementation uses a simplified movement model:
     * the elevator immediately reaches the requested floor.
     *
     * @param targetFloor requested destination floor
     *
     * @return true if movement was possible,
     *         false otherwise
     */
    public boolean moveToFloor(int targetFloor) {

        // Check that the target floor is valid.
        if (!isValidFloor(targetFloor)) {
            return false;
        }

        // IMPORTANT SAFETY RULE:
        // Elevator cannot move with open doors.
        if (areDoorsOpen()) {
            return false;
        }

        // Already at requested floor.
        if (targetFloor == currentFloor) {
            return true;
        }

        // Determine direction.
        if (targetFloor > currentFloor) {
            direction = Direction.MOVING_UP;
        } else {
            direction = Direction.MOVING_DOWN;
        }

        elevatorState = ElevatorState.MOVING;

        /*
         * Simplified simulation:
         * The elevator directly reaches the target floor.
         *
         * A more advanced version can later simulate:
         * floor-by-floor movement,
         * movement time,
         * intermediate stops, etc.
         */
        currentFloor = targetFloor;

        // Movement finished.
        elevatorState = ElevatorState.IDLE;
        direction = Direction.IDLE;

        return true;
    }

    /**
     * Stops the elevator.
     *
     * @return true if the elevator was moving and was stopped,
     *         false if it was already idle
     */
    public boolean stop() {

        if (!isMoving()) {
            return false;
        }

        elevatorState = ElevatorState.IDLE;
        direction = Direction.IDLE;

        return true;
    }


    // =========================================================
    // DESTINATION MANAGEMENT
    // =========================================================

    /**
     * Adds a requested destination.
     *
     * Duplicate destinations are not allowed.
     *
     * @param floor requested destination
     *
     * @return true if the destination was added,
     *         false otherwise
     */
    public boolean addDestination(int floor) {

        if (!isValidFloor(floor)) {
            return false;
        }

        if (destinations.contains(floor)) {
            return false;
        }

        destinations.add(floor);

        return true;
    }

    /**
     * Removes a destination.
     *
     * @param floor destination to remove
     *
     * @return true if the destination existed and was removed
     */
    public boolean removeDestination(int floor) {

        return destinations.remove(Integer.valueOf(floor));
    }

    /**
     * Checks whether a floor is currently a destination.
     *
     * @param floor floor to check
     *
     * @return true if the floor is a destination
     */
    public boolean hasDestination(int floor) {

        return destinations.contains(floor);
    }

    /**
     * Returns all current destinations.
     *
     * An unmodifiable list is returned so that external classes
     * cannot directly modify the elevator's internal state.
     *
     * @return list of destinations
     */
    public List<Integer> getDestinations() {

        return Collections.unmodifiableList(destinations);
    }

    /**
     * Removes all destinations.
     */
    public void clearDestinations() {

        destinations.clear();
    }

    /**
     * Returns the number of currently requested destinations.
     */
    public int getDestinationCount() {

        return destinations.size();
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Checks whether a floor number is valid.
     *
     * Valid floors are 1 through 150.
     *
     * @param floor floor to validate
     *
     * @return true if the floor is valid
     */
    public boolean isValidFloor(int floor) {

        return floor >= MIN_FLOOR && floor <= MAX_FLOOR;
    }


    // =========================================================
    // ELEVATOR AVAILABILITY
    // =========================================================

    /**
     * Determines whether the elevator is currently available
     * to accept a new passenger.
     *
     * The elevator must:
     * - be idle
     * - have its doors open
     * - not be full
     *
     * @return true if a passenger can enter
     */
    public boolean canAcceptPassenger() {

        return isIdle()
                && areDoorsOpen()
                && !isFull();
    }

    /**
     * Determines whether the elevator can start moving.
     *
     * The elevator:
     * - must not already be moving
     * - must have its doors closed
     *
     * @return true if the elevator can move
     */
    public boolean canMove() {

        return isIdle()
                && areDoorsClosed();
    }


    // =========================================================
    // RESET
    // =========================================================

    /**
     * Resets the elevator to a clean idle state at its
     * current floor.
     *
     * Useful for testing or simulation.
     */
    public void reset() {

        passengerCount = 0;

        direction = Direction.IDLE;

        doorState = DoorState.CLOSED;

        elevatorState = ElevatorState.IDLE;

        destinations.clear();
    }


    // =========================================================
    // STRING REPRESENTATION
    // =========================================================

    /**
     * Returns a readable representation of the elevator.
     */
    @Override
    public String toString() {

        return "Elevator{" +
                "id=" + id +
                ", currentFloor=" + currentFloor +
                ", passengerCount=" + passengerCount +
                ", direction=" + direction +
                ", doorState=" + doorState +
                ", elevatorState=" + elevatorState +
                ", destinations=" + destinations +
                '}';
            }
    
} 