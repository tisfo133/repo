package evaluation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.ElevatorEventDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class EvaluationApp {

    private static final int DEFAULT_PORT = 7001;
    private static final int MAX_PASSENGERS = 8;
    private static final int THREAD_POOL_SIZE = 8;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicInteger TOTAL_REQUESTS = new AtomicInteger(0);
    private static final AtomicInteger TOTAL_VIOLATIONS = new AtomicInteger(0);
    private static final AtomicInteger TOTAL_EVENTS = new AtomicInteger(0);

    private static final List<ElevatorEventDto> RECORDED_EVENTS = new CopyOnWriteArrayList<>();
    private static final Map<String, List<Double>> RECORDED_METRICS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("[EVALUATION WARNING] Μη έγκυρη θύρα. Χρήση προεπιλογής: " + DEFAULT_PORT);
            }
        }

        startServer(port);
    }

    // Εκκίνηση του HTTP server (η θύρα 0 επιλέγει αυτόματα ελεύθερη θύρα στα unit tests)
    static HttpServer startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/evaluation/metrics", new MetricsHandler());
        server.createContext("/api/evaluation/events", new EventsHandler());
        server.createContext("/api/evaluation/summary", new SummaryHandler());

        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
        server.start();
        System.out.println("[EVALUATION SYSTEM] Ο Server αξιολόγησης ξεκίνησε στη θύρα: "
                + server.getAddress().getPort());
        return server;
    }

    // Μηδενισμός μετρητών και λιστών για απομόνωση σε κάθε unit test
    static void resetMetrics() {
        TOTAL_REQUESTS.set(0);
        TOTAL_VIOLATIONS.set(0);
        TOTAL_EVENTS.set(0);
        RECORDED_EVENTS.clear();
        RECORDED_METRICS.clear();
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    static class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            int reqNum = TOTAL_REQUESTS.incrementAndGet();
            InputStream requestBody = exchange.getRequestBody();
            String jsonInput = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            System.out.println("[EVALUATION LOG] Αίτημα #" + reqNum + " - Δεδομένα: " + jsonInput);

            JsonNode root;
            try {
                root = MAPPER.readTree(jsonInput);
                if (root == null || !root.isObject()) {
                    throw new IllegalArgumentException("Κενό ή μη έγκυρο JSON object.");
                }
            } catch (Exception e) {
                System.out.println("[EVALUATION ERROR] Μη έγκυρο JSON: " + e.getMessage());
                sendJsonResponse(exchange, 400,
                        "{\"status\": \"ERROR\", \"message\": \"Invalid JSON payload\"}");
                return;
            }

            // 1. Αποθήκευση μετρικών απόδοσης από το EvaluationMetricsClient (metricName + value)
            if (root.has("metricName") && root.has("value") && root.get("value").isNumber()) {
                String metricName = root.get("metricName").asText();
                double value = root.get("value").asDouble();
                RECORDED_METRICS.computeIfAbsent(metricName, k -> new CopyOnWriteArrayList<>()).add(value);
                System.out.println("[EVALUATION METRIC] Καταγράφηκε " + metricName + " = " + value);
            }

            // 2. Έλεγχος χωρητικότητας επιβατών (όριο 8 άτομα)
            int passengers = 0;
            boolean isOverloaded = false;
            if (root.has("passengersOnboard") && root.get("passengersOnboard").isNumber()) {
                passengers = root.get("passengersOnboard").asInt();
                if (passengers > MAX_PASSENGERS) {
                    isOverloaded = true;
                    TOTAL_VIOLATIONS.incrementAndGet();
                }
            }

            String response;
            if (isOverloaded) {
                System.out.println("[EVALUATION ALERT] ΠΑΡΑΒΑΣΗ ΧΩΡΗΤΙΚΟΤΗΤΑΣ #" + TOTAL_VIOLATIONS.get()
                        + ": " + passengers + " επιβάτες (Όριο: " + MAX_PASSENGERS + ")");
                response = "{\"status\": \"FAILED\", \"violation\": \"CAPACITY_EXCEEDED\", "
                        + "\"message\": \"Overload detected! Max limit is " + MAX_PASSENGERS + " passengers.\"}";
            } else {
                System.out.println("[EVALUATION LOG] Επιτυχής αξιολόγηση. Επιβάτες εντός ορίων: " + passengers);
                response = "{\"status\": \"SUCCESS\", \"message\": \"Metrics recorded successfully. "
                        + "Total requests: " + reqNum + "\"}";
            }

            sendJsonResponse(exchange, 200, response);
        }
    }

    static class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String jsonInput = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                ElevatorEventDto event = MAPPER.readValue(jsonInput, ElevatorEventDto.class);
                RECORDED_EVENTS.add(event);
                int count = TOTAL_EVENTS.incrementAndGet();
                System.out.println("[EVALUATION EVENT #" + count + "] " + event);
                sendJsonResponse(exchange, 200,
                        "{\"status\": \"SUCCESS\", \"message\": \"Event recorded\", \"totalEvents\": " + count + "}");
            } catch (Exception e) {
                System.out.println("[EVALUATION ERROR] Μη έγκυρο ElevatorEventDto: " + e.getMessage());
                sendJsonResponse(exchange, 400,
                        "{\"status\": \"ERROR\", \"message\": \"Invalid ElevatorEventDto JSON\"}");
            }
        }
    }

    static class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String json = "{\"totalRequests\": " + TOTAL_REQUESTS.get()
                    + ", \"totalViolations\": " + TOTAL_VIOLATIONS.get()
                    + ", \"totalEvents\": " + TOTAL_EVENTS.get()
                    + ", \"recordedMetricTypes\": " + RECORDED_METRICS.size() + "}";
            sendJsonResponse(exchange, 200, json);
        }
    }
}