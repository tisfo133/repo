package elevator;

import common.Direction;
import common.ElevatorConfigDto;
import common.ElevatorEventDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HardwareBridgeRestClientTest {

    HardwareBridgeRestClient client = new HardwareBridgeRestClient("http://localhost:9999/fake");

    @Test
    void testSendElevatorEvent_HandlesNetworkError() {
        ElevatorEventDto event = new ElevatorEventDto('A', System.currentTimeMillis(), "FLOOR_REACHED", 1);
        assertFalse(client.sendElevatorEvent(event));
    }

    @Test
    void testSendConfiguration_HandlesNetworkError() {
        ElevatorConfigDto config = new ElevatorConfigDto(0, 10, 2500, 1500);
        assertFalse(client.sendConfiguration(config));
    }

    @Test
    void testHardwareCommands_DoNotCrashOnNetworkError() {
        assertDoesNotThrow(() -> client.openDoors());
        assertDoesNotThrow(() -> client.closeDoors());
        assertDoesNotThrow(() -> client.stop());
        assertDoesNotThrow(() -> client.move(Direction.DOWN));
        assertDoesNotThrow(() -> client.updateDisplay(3, Direction.UP));
    }
}