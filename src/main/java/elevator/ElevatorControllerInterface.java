package elevator;
import common.*;

public interface ElevatorControllerInterface {
    void initialize(char elevatorId, SystemConfig config);
    void addStop(int floor);
    ElevatorStatus getStatus();
    void displayStatusOnConsole();
    void reportMetricsToEvaluator();
}