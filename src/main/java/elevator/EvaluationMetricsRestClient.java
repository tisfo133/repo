package elevator;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ElevatorEventDto;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST client implementing outbound communication from an elevator
 * to the Evaluation System ({@code EvaluationApp}).
 */
public class EvaluationMetricsRestClient implements EvaluationMetricsClient {

  public static final String DEFAULT_EVALUATION_URL = "http://localhost:7001";
  private static final String DEFAULT_ELEVATOR_ID = "A";
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

  private final HttpClient httpClient;
  private final ObjectMapper mapper;
  private final String metricsUrl;
  private final String elevatorId;

  /**
   * Creates a client targeting the default evaluation server URL and elevator "A".
   */
  public EvaluationMetricsRestClient() {
    this(DEFAULT_EVALUATION_URL, DEFAULT_ELEVATOR_ID);
  }

  /**
   * Creates a client targeting the given evaluation server URL for elevator "A".
   *
   * @param metricsUrl base URL of the Evaluation System (e.g. "http://localhost:7001")
   */
  public EvaluationMetricsRestClient(String metricsUrl) {
    this(metricsUrl, DEFAULT_ELEVATOR_ID);
  }

  /**
   * Creates a client targeting the given evaluation server URL and elevator ID.
   *
   * @param metricsUrl base URL of the Evaluation System
   * @param elevatorId elevator identifier (e.g. "A", "B")
   */
  public EvaluationMetricsRestClient(String metricsUrl, String elevatorId) {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(REQUEST_TIMEOUT)
        .build();
    this.mapper = new ObjectMapper();
    String base = (metricsUrl == null || metricsUrl.isBlank())
        ? DEFAULT_EVALUATION_URL
        : metricsUrl.trim();
    this.metricsUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    this.elevatorId = (elevatorId == null || elevatorId.isBlank())
        ? DEFAULT_ELEVATOR_ID
        : elevatorId.trim();
  }

  @Override
  public void sendEvent(ElevatorEventDto event) {
    if (event == null) {
      System.out.println("[EVALUATION CLIENT] Ignored null ElevatorEventDto.");
      return;
    }
    try {
      String json = mapper.writeValueAsString(event);
      postJson("/api/evaluation/events", json);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("[EVALUATION CLIENT] Interrupted while sending event: "
          + e.getMessage());
    } catch (IOException | IllegalArgumentException e) {
      System.out.println("[EVALUATION CLIENT] Failed to send event: " + e.getMessage());
    }
  }

  @Override
  public void sendMetric(String metricName, double value) {
    if (metricName == null || metricName.isBlank() || !Double.isFinite(value)) {
      System.out.println("[EVALUATION CLIENT] Ignored invalid metric payload.");
      return;
    }
    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("elevatorId", elevatorId);
      payload.put("metricName", metricName);
      payload.put("value", value);

      String json = mapper.writeValueAsString(payload);
      postJson("/api/evaluation/metrics", json);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("[EVALUATION CLIENT] Interrupted while sending metric "
          + metricName + ": " + e.getMessage());
    } catch (IOException | IllegalArgumentException e) {
      System.out.println("[EVALUATION CLIENT] Failed to send metric "
          + metricName + ": " + e.getMessage());
    }
  }

  @Override
  public void sendTelemetry(String action, int currentFloor, int passengersOnboard) {
    String currentTime = LocalTime.now().format(TIME_FORMATTER);
    sendTelemetry(this.elevatorId, currentTime, action, currentFloor, passengersOnboard);
  }

  @Override
  public void sendTelemetry(
      String targetElevatorId,
      String timestamp,
      String action,
      int currentFloor,
      int passengersOnboard) {
    if (action == null || action.isBlank() || passengersOnboard < 0) {
      System.out.println("[EVALUATION CLIENT] Ignored invalid telemetry payload.");
      return;
    }
    String resolvedId = (targetElevatorId == null || targetElevatorId.isBlank())
        ? this.elevatorId
        : targetElevatorId.trim();
    String resolvedTime = (timestamp == null || timestamp.isBlank())
        ? LocalTime.now().format(TIME_FORMATTER)
        : timestamp.trim();

    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("elevatorId", resolvedId);
      payload.put("timestamp", resolvedTime);
      payload.put("action", action);
      payload.put("currentFloor", currentFloor);
      payload.put("passengersOnboard", passengersOnboard);

      String json = mapper.writeValueAsString(payload);
      postJson("/api/evaluation/metrics", json);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("[EVALUATION CLIENT] Interrupted while sending telemetry: "
          + e.getMessage());
    } catch (IOException | IllegalArgumentException e) {
      System.out.println("[EVALUATION CLIENT] Failed to send telemetry: " + e.getMessage());
    }
  }

  @Override
  public void sendWaitTimeMetric(double waitTimeSeconds) {
    sendMetric("AVERAGE_WAIT_TIME_SEC", waitTimeSeconds);
  }

  @Override
  public void sendJourneyTimeMetric(double journeyTimeSeconds) {
    sendMetric("AVERAGE_JOURNEY_TIME_SEC", journeyTimeSeconds);
  }

  @Override
  public void sendEnergyEfficiencyMetric(int totalFloorsMoved) {
    sendMetric("TOTAL_FLOORS_MOVED", totalFloorsMoved);
  }

  @Override
  public String getEvaluationSummary() {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(metricsUrl + "/api/evaluation/summary"))
          .timeout(REQUEST_TIMEOUT)
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(
          request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      int status = response.statusCode();
      if (status >= 200 && status < 300) {
        return response.body();
      }
      System.out.println("[EVALUATION CLIENT] Summary request failed (HTTP " + status + ").");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("[EVALUATION CLIENT] Interrupted while fetching summary: "
          + e.getMessage());
    } catch (IOException | IllegalArgumentException e) {
      System.out.println("[EVALUATION CLIENT] Failed to fetch summary: " + e.getMessage());
    }
    return null;
  }

  private void postJson(String endpoint, String jsonPayload)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(metricsUrl + endpoint))
        .timeout(REQUEST_TIMEOUT)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> response = httpClient.send(
        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    int status = response.statusCode();
    String body = response.body();

    if (status < 200 || status >= 300) {
      System.out.println("[EVALUATION CLIENT] HTTP " + status
          + " at " + endpoint + " -> " + body);
    } else if (body != null && body.contains("CAPACITY_EXCEEDED")) {
      System.out.println("[EVALUATION CLIENT ALERT] Capacity violation reported: " + body);
    } else {
      System.out.println("[EVALUATION CLIENT] Sent OK (" + status + ") to " + endpoint);
    }
  }
}