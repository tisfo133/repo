package elevator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * REST client for sending elevator events and metrics to the Evaluation system.
 */
public class HttpEvaluationMetricsClient implements EvaluationMetricsClient {

  private static final String DEFAULT_EVALUATION_URL = "http://localhost:7001";
  private static final String EVENTS_ENDPOINT = "/api/evaluation/events";
  private static final String METRICS_ENDPOINT = "/api/evaluation/metrics";
  private static final int CONNECT_TIMEOUT_SECONDS = 3;

  private final String evaluationBaseUrl;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final char elevatorId;

  /**
   * Creates a client using the default Evaluation URL.
   *
   * @param elevatorId elevator identifier
   */
  public HttpEvaluationMetricsClient(char elevatorId) {
    this(elevatorId, DEFAULT_EVALUATION_URL);
  }

  /**
   * Creates a client using the provided Evaluation URL.
   *
   * @param elevatorId elevator identifier
   * @param evaluationBaseUrl Evaluation base URL
   */
  public HttpEvaluationMetricsClient(
      char elevatorId, String evaluationBaseUrl) {

    this.elevatorId = elevatorId;
    this.evaluationBaseUrl = normalizeBaseUrl(evaluationBaseUrl);

    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();

    this.objectMapper = new ObjectMapper();
  }

  /**
   * Normalizes the Evaluation base URL.
   *
   * @param baseUrl Evaluation base URL
   * @return normalized base URL
   */
  private static String normalizeBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return DEFAULT_EVALUATION_URL;
    }

    return baseUrl.endsWith("/")
        ? baseUrl.substring(0, baseUrl.length() - 1)
        : baseUrl;
  }

  /**
   * Sends an elevator event to the Evaluation system.
   *
   * @param event elevator event
   */
  @Override
  public void sendEvent(ElevatorEventDto event) {
    if (event == null) {
      return;
    }

    try {
      String jsonBody = objectMapper.writeValueAsString(event);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(evaluationBaseUrl + EVENTS_ENDPOINT))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      System.out.println(
          "[ELEVATOR] Evaluation event sent. HTTP "
              + response.statusCode());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      System.out.println(
          "[ELEVATOR] Interrupted while sending evaluation event: "
              + e.getMessage());

    } catch (IOException | IllegalArgumentException e) {
      System.out.println(
          "[ELEVATOR] Failed to send evaluation event: "
              + e.getMessage());
    }
  }

  /**
   * Sends an elevator performance metric to the Evaluation system.
   *
   * @param metricName metric name
   * @param value metric value
   */
  @Override
  public void sendMetric(String metricName, double value) {
    if (metricName == null || metricName.isBlank()) {
      return;
    }

    try {
      MetricRequest metric =
          new MetricRequest(
              String.valueOf(elevatorId),
              metricName,
              value);

      String jsonBody = objectMapper.writeValueAsString(metric);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(evaluationBaseUrl + METRICS_ENDPOINT))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      System.out.println(
          "[ELEVATOR] Evaluation metric sent. HTTP "
              + response.statusCode());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      System.out.println(
          "[ELEVATOR] Interrupted while sending evaluation metric: "
              + e.getMessage());

    } catch (IOException | IllegalArgumentException e) {
      System.out.println(
          "[ELEVATOR] Failed to send evaluation metric: "
              + e.getMessage());
    }
  }

  /**
   * Returns the configured Evaluation base URL.
   *
   * @return Evaluation base URL
   */
  public String getEvaluationBaseUrl() {
    return evaluationBaseUrl;
  }

  private record MetricRequest(
      String elevatorId,
      String metricName,
      double value) {
  }
}