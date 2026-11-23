package central;

import common.DestinationCallDTO;
import common.Direction;
import common.DoorState;
import common.ElevatorStateDTO;
import common.SystemConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CentralServiceTest {

    @Test
    void initializesCorrectNumberOfElevatorsAndPorts() {
        SystemConfig config = new SystemConfig(0, 12, 3, 7000);
        CentralService service = new CentralService(config);

        assertEquals(3, service.getAllElevators().size());
        assertEquals("http://localhost:7002", service.getElevator("A").getBaseUrl());
        assertEquals("http://localhost:7003", service.getElevator("B").getBaseUrl());
        assertEquals("http://localhost:7004", service.getElevator("C").getBaseUrl());
    }

    @Test
    void updatesStateFromDtoAndDispatchesToCloserElevator() {
        SystemConfig config = new SystemConfig(0, 10, 2, 7000);
        CentralService service = new CentralService(config);

        ElevatorStateDTO stateB = new ElevatorStateDTO("B", 9, Direction.IDLE, DoorState.CLOSED, List.of());
        service.updateElevatorState("B", stateB);

        ElevatorInfo assigned = service.assignCall(new DestinationCallDTO(8, 2));
        assertEquals("B", assigned.getElevatorId());
    }

    @Test
    void avoidsFullCapacityElevator() {
        SystemConfig config = new SystemConfig(0, 10, 2, 7000);
        CentralService service = new CentralService(config);

        service.getElevator("A").setCurrentFloor(5);
        service.getElevator("A").setPassengerCount(ElevatorInfo.MAX_PASSENGERS);
        service.getElevator("B").setCurrentFloor(1);

        ElevatorInfo assigned = service.assignCall(new DestinationCallDTO(5, 8));
        assertEquals("B", assigned.getElevatorId());
    }

    @Test
    void rejectsInvalidFloors() {
        SystemConfig config = new SystemConfig(0, 10, 3, 7000);
        CentralService service = new CentralService(config);

        assertThrows(IllegalArgumentException.class,
                () -> service.assignCall(new DestinationCallDTO(0, 15)));
    }
}
