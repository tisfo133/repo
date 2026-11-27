package elevator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import common.ElevatorEventDto;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluationMetricsRestClientTest {

  private HttpServer mockServer;
  private String baseUrl;
  private final ObjectMapper mapper = new ObjectMapper();
  private final AtomicReference<String> lastEventsBody = new AtomicReference<>();
  private final AtomicReference<String> lastMetricsBody = new AtomicReference<>();
  private final AtomicReference<String> lastContentType = new AtomicReference<>();
  private final AtomicInteger nextStatusCode = new AtomicInteger(200);
  private final AtomicReference<String> nextMetricsResponse =
      new AtomicReference<>("{\"status\":\"SUCCESS\",\"message\":\"Metrics recorded\"}");

  @BeforeEach
  void setUp() throws IOException {
    lastEventsBody.set(null);
    lastMetricsBody.set(null);
    lastContentType.set(null);
    nextStatusCode.set(200);
    nextMetricsResponse.set("{\"status\":\"SUCCESS\",\"message\":\"Metrics recorded\"}");

    mockServer = HttpServer.create(new InetSocketAddress(0), 0);

    mockServer.createContext("/api/evaluation/events", exchange -> {
      lastContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
      String body = new String(
          exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      lastEventsBody.set(body);
      byte[] resp = "{\"status\":\"SUCCESS\",\"message\":\"Event recorded\",\"totalEvents\":1}"
          .getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(nextStatusCode.get(), resp.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(resp);
      }
    });

    mockServer.createContext("/api/evaluation/metrics", exchange -> {
      lastContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
      String body = new String(
          exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      lastMetricsBody.set(body);
      byte[] resp = nextMetricsResponse.get().getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(nextStatusCode.get(), resp.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(resp);
      }
    });

    mockServer.createContext("/api/evaluation/summary", exchange -> {
      byte[] resp = ("{\"totalRequests\":2,\"totalViolations\":1,"
          + "\"totalEvents\":1,\"recordedMetricTypes\":1}")
          .getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(nextStatusCode.get(), resp.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(resp);
      }
    });

    mockServer.start();
    baseUrl = "http://localhost:" + mockServer.getAddress().getPort();
  }

  @AfterEach
  void tearDown() {
    if (mockServer != null) {
      mockServer.stop(0);
    }
  }

  @Test
  void testSendEventProducesValidElevatorEventDtoJson() throws Exception {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl + "/", "A");
    ElevatorEventDto event = new ElevatorEventDto('A', 1730712000000L, "FLOOR_REACHED", 4);

    client.sendEvent(event);

    assertNotNull(lastEventsBody.get());
    assertEquals("application/json", lastContentType.get());
    ElevatorEventDto parsed = mapper.readValue(lastEventsBody.get(), ElevatorEventDto.class);
    assertEquals('A', parsed.elevatorId());
    assertEquals(1730712000000L, parsed.timestamp());
    assertEquals("FLOOR_REACHED", parsed.eventType());
    assertEquals(4, parsed.floor());
  }

  @Test
  void testSendMetricProducesValidMetricJson() throws Exception {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl);

    client.sendMetric("DOOR_OPERATION_TIME_MS", 1500.0);

    assertNotNull(lastMetricsBody.get());
    assertEquals("application/json", lastContentType.get());
    JsonNode root = mapper.readTree(lastMetricsBody.get());
    assertEquals("A", root.get("elevatorId").asText());
    assertEquals("DOOR_OPERATION_TIME_MS", root.get("metricName").asText());
    assertEquals(1500.0, root.get("value").asDouble(), 0.001);
  }

  @Test
  void testMetricWrappersSendExpectedMetricNamesAndValues() throws Exception {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "A");

    client.sendWaitTimeMetric(12.5);
    JsonNode waitNode = mapper.readTree(lastMetricsBody.get());
    assertEquals("AVERAGE_WAIT_TIME_SEC", waitNode.get("metricName").asText());
    assertEquals(12.5, waitNode.get("value").asDouble(), 0.001);

    client.sendJourneyTimeMetric(24.0);
    JsonNode journeyNode = mapper.readTree(lastMetricsBody.get());
    assertEquals("AVERAGE_JOURNEY_TIME_SEC", journeyNode.get("metricName").asText());
    assertEquals(24.0, journeyNode.get("value").asDouble(), 0.001);

    client.sendEnergyEfficiencyMetric(18);
    JsonNode energyNode = mapper.readTree(lastMetricsBody.get());
    assertEquals("TOTAL_FLOORS_MOVED", energyNode.get("metricName").asText());
    assertEquals(18.0, energyNode.get("value").asDouble(), 0.001);
  }

  @Test
  void testSendTelemetryMatchesApiSpecsAndHandlesOverloadResponse() throws Exception {
    nextMetricsResponse.set(
        "{\"status\":\"FAILED\",\"violation\":\"CAPACITY_EXCEEDED\"}");
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "B");

    client.sendTelemetry("B", "08:00:25", "DOORS_CLOSED", 0, 9);

    assertNotNull(lastMetricsBody.get());
    JsonNode root = mapper.readTree(lastMetricsBody.get());
    assertEquals("B", root.get("elevatorId").asText());
    assertEquals("08:00:25", root.get("timestamp").asText());
    assertEquals("DOORS_CLOSED", root.get("action").asText());
    assertEquals(0, root.get("currentFloor").asInt());
    assertEquals(9, root.get("passengersOnboard").asInt());
  }

  @Test
  void testSendTelemetryWithAutoTimestampProducesValidFormat() throws Exception {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "A");

    client.sendTelemetry("DOORS_OPENED", 3, 4);

    assertNotNull(lastMetricsBody.get());
    JsonNode root = mapper.readTree(lastMetricsBody.get());
    assertEquals("A", root.get("elevatorId").asText());
    assertTrue(root.get("timestamp").asText().matches("\\d{2}:\\d{2}:\\d{2}"));
    assertEquals("DOORS_OPENED", root.get("action").asText());
    assertEquals(3, root.get("currentFloor").asInt());
    assertEquals(4, root.get("passengersOnboard").asInt());
  }

  @Test
  void testGetEvaluationSummaryReturnsServerJson() {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "A");

    String summary = client.getEvaluationSummary();

    assertNotNull(summary);
    assertTrue(summary.contains("\"totalViolations\":1"));
  }

  @Test
  void testNonSuccessHttpResponsesAreHandledGracefully() {
    nextStatusCode.set(500);
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "A");
    ElevatorEventDto event = new ElevatorEventDto('A', 1730712000000L, "FLOOR_REACHED", 2);

    assertDoesNotThrow(() -> client.sendEvent(event));
    assertDoesNotThrow(() -> client.sendMetric("AVERAGE_WAIT_TIME_SEC", 8.0));
    assertNull(client.getEvaluationSummary());
  }

  @Test
  void testInvalidInputsAreIgnoredWithoutSendingHttpRequests() {
    EvaluationMetricsClient client = new EvaluationMetricsRestClient(baseUrl, "A");

    client.sendEvent(null);
    client.sendMetric(null, 5.0);
    client.sendMetric("   ", 5.0);
    client.sendMetric("WAIT_TIME", Double.NaN);
    client.sendTelemetry("A", "08:00:25", "", 0, 2);
    client.sendTelemetry("A", "08:00:25", "DOORS_CLOSED", 0, -1);

    assertNull(lastEventsBody.get());
    assertNull(lastMetricsBody.get());
  }

  @Test
  void testDefaultConstructorUsesSafeDefaults() {
    EvaluationMetricsRestClient defaultClient = new EvaluationMetricsRestClient();
    EvaluationMetricsRestClient fallbackClient = new EvaluationMetricsRestClient("  ", " ");
    assertNotNull(defaultClient);
    assertNotNull(fallbackClient);
  }

  @Test
  void testCommandsDoNotCrashOnNetworkError() {
    EvaluationMetricsClient offlineClient =
        new EvaluationMetricsRestClient("http://localhost:9999", "A");
    ElevatorEventDto event = new ElevatorEventDto('A', 1730712000000L, "DOORS_OPENED", 2);

    assertDoesNotThrow(() -> offlineClient.sendEvent(event));
    assertDoesNotThrow(() -> offlineClient.sendMetric("AVERAGE_WAIT_TIME_SEC", 10.0));
    assertDoesNotThrow(() -> offlineClient.sendTelemetry("A", "08:00:25", "DOORS_CLOSED", 2, 3));
    assertNull(offlineClient.getEvaluationSummary());
  }
}