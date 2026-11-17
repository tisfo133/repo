package elevator;

import common.ElevatorEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Υλοποίηση για την αποστολή δεδομένων στο σύστημα Metrics.
 */
public class EvaluationMetricsRestClient implements EvaluationMetricsClient {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String metricsUrl;
    private final String elevatorId = "A";

    public EvaluationMetricsRestClient(String metricsUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.metricsUrl = metricsUrl;
    }

    @Override
    public void sendEvent(ElevatorEventDto event) {
        try {
            // Φτιάχνουμε το JSON που ζητάει το interface (με elevatorId, timestamp και το event)
            Map<String, Object> payload = new HashMap<>();
            payload.put("elevatorId", elevatorId);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("event", event);

            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(metricsUrl + "/api/evaluation/events"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.out.println("Αποτυχία καταγραφής συμβάντος στα Metrics.");
        }
    }

    @Override
    public void sendMetric(String metricName, double value) {
        try {
            // Φτιάχνουμε το JSON για τα μεμονωμένα metrics (π.χ. χρόνος ανοίγματος πόρτας)
            Map<String, Object> payload = new HashMap<>();
            payload.put("elevatorId", elevatorId);
            payload.put("metricName", metricName);
            payload.put("value", value);

            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(metricsUrl + "/api/evaluation/metrics"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.out.println("Αποτυχία καταγραφής μετρικής: " + metricName);
        }
    }
}
