package elevator;
import common.ElevatorEventDto;
/* Εξερχόμενη διεπαφή (Outbound REST Client Contract)για την 
αποστολή συμβάντων και μετρικών απόδοσης προσ το σύστημα
δοκιμών και το σύστημα Αξιολόγησης*/
public interface EvaluationMetricsClient {
    /*
    Endpoint: POST /api/evaluation/events
    JSON Request: { "elevatorId": "A", "timestamp": 1730712000000, "eventType": "FLOOR_REACHED", "floor": 4 }
    */
    void sendEvent(ElevatorEventDto event);
    /*
    Endpoint: POST /api/evaluation/metrics
    JSON Request: { "elevatorId": "A", "metricName": "DOOR_OPERATION_TIME_MS", "value": 1500.0 }
    */
    void sendMetric(String metricName, double value);
}