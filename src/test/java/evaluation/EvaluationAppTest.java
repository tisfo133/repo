package evaluation;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationAppTest {

    private HttpServer server;
    private HttpClient client;
    private String baseHost;
    private String metricsUrl;

    @BeforeEach
    void setUp() throws Exception {
        EvaluationApp.resetMetrics();
        server = EvaluationApp.startServer(0);
        int actualPort = server.getAddress().getPort();
        baseHost = "http://localhost:" + actualPort;
        metricsUrl = baseHost + "/api/evaluation/metrics";
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldAcceptNormalPassengerCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"passengersOnboard\": 5}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"SUCCESS\""));
    }

    @Test
    void shouldAllowExactCapacityLimitOfEightPassengers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"passengersOnboard\": 8}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"SUCCESS\""));
    }

    @Test
    void shouldDetectCapacityViolationAboveEightPassengers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"passengersOnboard\": 9}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"FAILED\""));
        assertTrue(response.body().contains("CAPACITY_EXCEEDED"));
    }

    @Test
    void shouldReturnMethodNotAllowedForGetRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    @Test
    void shouldRecordPerformanceMetricFromElevator() throws Exception {
        String payload = "{\"elevatorId\": \"A\", \"metricName\": \"DOOR_OPERATION_TIME_MS\", \"value\": 1500.0}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\": \"SUCCESS\""));
    }

    @Test
    void shouldReturnBadRequestForInvalidJson() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metricsUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{invalid-json"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRecordElevatorEvent() throws Exception {
        String payload = "{\"elevatorId\": \"A\", \"timestamp\": 1730712000000, \"eventType\": \"FLOOR_REACHED\", \"floor\": 4}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseHost + "/api/evaluation/events"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"totalEvents\": 1"));
    }

    @Test
    void shouldReturnEvaluationSummary() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseHost + "/api/evaluation/summary"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"totalRequests\": 0"));
    }
}