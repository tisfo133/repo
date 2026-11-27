package elevator;

import common.ElevatorEventDto;

/**
 * Outbound REST client contract for sending events, performance metrics,
 * and telemetry from an elevator to the Evaluation System.
 */
public interface EvaluationMetricsClient {

  /**
   * Sends an elevator operational event to {@code POST /api/evaluation/events}.
   *
   * @param event the elevator event payload
   */
  void sendEvent(ElevatorEventDto event);

  /**
   * Sends a named performance metric to {@code POST /api/evaluation/metrics}.
   *
   * @param metricName the metric identifier
   * @param value the numeric metric value
   */
  void sendMetric(String metricName, double value);

  /**
   * Sends telemetry data to {@code POST /api/evaluation/metrics} per api-specs.md.
   *
   * @param elevatorId elevator identifier (e.g. "A", "B")
   * @param timestamp time formatted as HH:mm:ss
   * @param action action label (e.g. "DOORS_CLOSED")
   * @param currentFloor current floor of the elevator
   * @param passengersOnboard number of passengers currently inside the cabin
   */
  default void sendTelemetry(
      String elevatorId,
      String timestamp,
      String action,
      int currentFloor,
      int passengersOnboard) {
    // Default no-op for backward compatibility with existing implementations.
  }

  /**
   * Sends telemetry data using the client's elevator ID and current local time.
   *
   * @param action action label (e.g. "DOORS_CLOSED")
   * @param currentFloor current floor of the elevator
   * @param passengersOnboard number of passengers currently inside the cabin
   */
  default void sendTelemetry(String action, int currentFloor, int passengersOnboard) {
    // Default no-op for backward compatibility with existing implementations.
  }

  /**
   * Sends the passenger average waiting time metric in seconds.
   *
   * @param waitTimeSeconds waiting time in seconds
   */
  default void sendWaitTimeMetric(double waitTimeSeconds) {
    sendMetric("AVERAGE_WAIT_TIME_SEC", waitTimeSeconds);
  }

  /**
   * Sends the passenger average journey time metric in seconds.
   *
   * @param journeyTimeSeconds journey time in seconds
   */
  default void sendJourneyTimeMetric(double journeyTimeSeconds) {
    sendMetric("AVERAGE_JOURNEY_TIME_SEC", journeyTimeSeconds);
  }

  /**
   * Sends the total floors moved metric as an energy efficiency indicator.
   *
   * @param totalFloorsMoved total floors traversed
   */
  default void sendEnergyEfficiencyMetric(int totalFloorsMoved) {
    sendMetric("TOTAL_FLOORS_MOVED", totalFloorsMoved);
  }

  /**
   * Fetches the evaluation summary JSON from {@code GET /api/evaluation/summary}.
   *
   * @return raw summary JSON string, or {@code null} if unavailable
   */
  default String getEvaluationSummary() {
    return null;
  }
}