package central;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElevatorRestClientTest {

    @Test
    void handlesOfflineElevatorGracefully() {
        ElevatorRestClient client = new ElevatorRestClient();
        ElevatorInfo offlineElevator = new ElevatorInfo("Z", "http://localhost:9999", 0);

        assertFalse(client.sendStopCommand(offlineElevator, 5));
        assertNotNull(client.fetchElevatorState(offlineElevator));
        assertDoesNotThrow(() -> client.fetchElevatorState(offlineElevator));
    }
}
