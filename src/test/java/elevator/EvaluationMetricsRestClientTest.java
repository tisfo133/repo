package elevator;

import common.ElevatorEventDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EvaluationMetricsRestClientTest {

    @Test
    void testMetricsCommands_DoNotCrashOnNetworkError() {
        EvaluationMetricsRestClient client = new EvaluationMetricsRestClient("http://localhost:9999/fake");
        ElevatorEventDto event = new ElevatorEventDto('A', System.currentTimeMillis(), "FLOOR_REACHED", 1);

        assertDoesNotThrow(() -> client.sendEvent(event));
        assertDoesNotThrow(() -> client.sendMetric("DOOR_OPERATION_TIME_MS", 1200.5));
    }
}