package elevator;

import common.ElevatorConfigDto;
import common.ElevatorEventDto;
import common.Direction; // Προστέθηκε για τη μέθοδο move()
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HardwareBridgeRestClient implements HardwareBridgeClient {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public HardwareBridgeRestClient(String baseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean sendElevatorEvent(ElevatorEventDto event) {
        try {
            String json = mapper.writeValueAsString(event);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/hardware/event"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.out.println("Πρόβλημα στο sendElevatorEvent.");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendConfiguration(ElevatorConfigDto config) {
        try {
            String json = mapper.writeValueAsString(config);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/hardware/config"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.out.println("Πρόβλημα στο sendConfiguration.");
            e.printStackTrace();
            return false;
        }
    }

    // --- ΟΙ 5 ΜΕΘΟΔΟΙ ΤΗΣ ΟΜΑΔΑΣ ΠΟΥ ΕΛΕΙΠΑΝ ---

    @Override
    public void openDoors() {
        System.out.println("Sending open doors command to hardware...");
    }

    @Override
    public void closeDoors() {
        System.out.println("Sending close doors command to hardware...");
    }

    @Override
    public void stop() {
        System.out.println("Sending stop command to hardware...");
    }

    @Override
    public void move(Direction direction) {
        System.out.println("Sending move command: " + direction);
    }

    @Override
    public void updateDisplay(int floor, Direction direction) {
        System.out.println("Updating display: Floor " + floor + ", Direction: " + direction);
    }
}