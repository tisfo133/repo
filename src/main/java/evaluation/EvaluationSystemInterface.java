package evaluation;
import common.*;

public interface EvaluationSystemInterface {
    void loadScenario(String scenarioFilePath);
    void runSimulationScenario();
    void recordElevatorEvent(ElevatorStatus status, long timestamp);
    void displayEvaluationResults();
}