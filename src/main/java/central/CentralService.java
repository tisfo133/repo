package central;

import common.DestinationCallDTO;
import common.Direction;
import common.ElevatorStateDTO;
import common.SystemConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Κεντρική υπηρεσία διαχείρισης στόλου ανελκυστήρων και δρομολόγησης κλήσεων.
 */
public class CentralService {

    private static final int BASE_ELEVATOR_PORT = 7002;
    private static final int FULL_CAPACITY_PENALTY = 10_000;
    private static final int WRONG_DIRECTION_PENALTY = 15;
    private static final int STOP_QUEUE_WEIGHT = 2;

    private volatile SystemConfig config;
    private final Map<String, ElevatorInfo> elevators;

    public CentralService() {
        this(new SystemConfig());
    }

    public CentralService(SystemConfig config) {
        this.elevators = new ConcurrentHashMap<>();
        configure(config);
    }

    public synchronized void configure(SystemConfig newConfig) {
        if (newConfig == null) {
            throw new IllegalArgumentException("Το SystemConfig δεν μπορεί να είναι null.");
        }
        this.config = newConfig;
        this.elevators.clear();

        for (int i = 0; i < newConfig.getNumElevators(); i++) {
            String id = String.valueOf((char) ('A' + i));
            int port = BASE_ELEVATOR_PORT + i;
            String url = "http://localhost:" + port;
            elevators.put(id, new ElevatorInfo(id, url, newConfig.getMinFloor()));
        }
    }

    public ElevatorInfo assignCall(DestinationCallDTO call) {
        if (call == null) {
            throw new IllegalArgumentException("Το αίτημα κλήσης δεν μπορεί να είναι null.");
        }
        SystemConfig current = this.config;
        if (!current.isFloorValid(call.getFromFloor()) || !current.isFloorValid(call.getToFloor())) {
            throw new IllegalArgumentException("Μη έγκυρος όροφος για τις τρέχουσες ρυθμίσεις.");
        }
        if (call.getFromFloor() == call.getToFloor()) {
            throw new IllegalArgumentException("Ο όροφος αφετηρίας και προορισμού δεν μπορεί να είναι ίδιος.");
        }

        ElevatorInfo chosen = selectBestElevator(call);
        chosen.assignTrip(call.getFromFloor(), call.getToFloor());
        return chosen;
    }

    private ElevatorInfo selectBestElevator(DestinationCallDTO call) {
        ElevatorInfo bestElevator = null;
        int lowestCost = Integer.MAX_VALUE;

        for (ElevatorInfo elevator : elevators.values()) {
            int cost = calculateCost(elevator, call);
            if (cost < lowestCost) {
                lowestCost = cost;
                bestElevator = elevator;
            }
        }
        return bestElevator;
    }

    int calculateCost(ElevatorInfo elevator, DestinationCallDTO call) {
        int fromFloor = call.getFromFloor();
        Direction callDir = call.getDirection();
        int currentFloor = elevator.getCurrentFloor();
        Direction elevDir = elevator.getDirection();
        int distance = Math.abs(currentFloor - fromFloor);

        int cost = distance + (elevator.getNextStops().size() * STOP_QUEUE_WEIGHT);

        if (!elevator.hasAvailableCapacity()) {
            cost += FULL_CAPACITY_PENALTY;
        }

        if (elevDir == Direction.IDLE) {
            return cost;
        }

        boolean movingTowardsCall =
                (elevDir == Direction.UP && currentFloor <= fromFloor)
                || (elevDir == Direction.DOWN && currentFloor >= fromFloor);

        if (movingTowardsCall && elevDir == callDir) {
            cost -= 1;
        } else if (!movingTowardsCall || elevDir != callDir) {
            cost += WRONG_DIRECTION_PENALTY;
        }

        return Math.max(0, cost);
    }

    public void updateElevatorState(String elevatorId, ElevatorStateDTO stateDto) {
        ElevatorInfo info = elevators.get(elevatorId);
        if (info != null) {
            info.updateFromStateDto(stateDto);
        }
    }

    public SystemConfig getConfig() {
        return config;
    }

    public Collection<ElevatorInfo> getAllElevators() {
        return Collections.unmodifiableCollection(elevators.values());
    }

    public ElevatorInfo getElevator(String id) {
        return elevators.get(id);
    }
}
