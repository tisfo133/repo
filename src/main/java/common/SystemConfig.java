package common;

public class SystemConfig {

    public static final int ABSOLUTE_MIN_ELEVATORS = 2;
    public static final int ABSOLUTE_MAX_ELEVATORS = 20;
    public static final int ABSOLUTE_MIN_FLOORS = 3;
    public static final int ABSOLUTE_MAX_FLOORS = 200;

    public static final int DEFAULT_MIN_FLOOR = 0;
    public static final int DEFAULT_MAX_FLOOR = 3;
    public static final int DEFAULT_NUM_ELEVATORS = 4;
    public static final int DEFAULT_CENTRAL_PORT = 7000;

    private final int minFloor;
    private final int maxFloor;
    private final int numElevators;
    private final int centralPort;

    public SystemConfig() {
        this(DEFAULT_MIN_FLOOR, DEFAULT_MAX_FLOOR, DEFAULT_NUM_ELEVATORS, DEFAULT_CENTRAL_PORT);
    }

    public SystemConfig(int minFloor, int maxFloor, int numElevators, int centralPort) {
        validate(minFloor, maxFloor, numElevators, centralPort);
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.numElevators = numElevators;
        this.centralPort = centralPort;
    }

    private void validate(int minFloor, int maxFloor, int numElevators, int centralPort) {
        int totalFloors = maxFloor - minFloor + 1;
        if (totalFloors < ABSOLUTE_MIN_FLOORS || totalFloors > ABSOLUTE_MAX_FLOORS) {
            throw new IllegalArgumentException("Invalid total floors range: " + totalFloors);
        }
        if (numElevators < ABSOLUTE_MIN_ELEVATORS || numElevators > ABSOLUTE_MAX_ELEVATORS) {
            throw new IllegalArgumentException("Invalid number of elevators: " + numElevators);
        }
        if (centralPort <= 1024 || centralPort > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + centralPort);
        }
    }

    public int getMinFloor() {
        return minFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public int getNumElevators() {
        return numElevators;
    }

    public int getCentralPort() {
        return centralPort;
    }

    public boolean isFloorValid(int floor) {
        return floor >= minFloor && floor <= maxFloor;
    }
}
