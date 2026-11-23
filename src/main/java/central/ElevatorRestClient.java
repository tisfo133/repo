package central;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ElevatorCommandDTO;
import common.ElevatorStateDTO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * REST client για την αποστολή εντολών και την ανάκτηση κατάστασης από τους ανελκυστήρες.
 */
public class ElevatorRestClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ElevatorRestClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean sendStopCommand(ElevatorInfo elevator, int targetFloor) {
        try {
            String url = elevator.getBaseUrl() + "/api/elevator/" + elevator.getElevatorId() + "/stop";
            ElevatorCommandDTO command = new ElevatorCommandDTO(elevator.getElevatorId(), targetFloor);
            String jsonBody = objectMapper.writeValueAsString(command);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            if (success) {
                System.out.println("Δόθηκε εντολή στο ασανσέρ " + elevator.getElevatorId()
                        + " (" + elevator.getBaseUrl() + ") -> όροφος " + targetFloor);
            }
            return success;
        } catch (Exception e) {
            System.out.println("Σφάλμα επικοινωνίας με το ασανσέρ " + elevator.getElevatorId() + ": " + e.getMessage());
            return false;
        }
    }

    public Optional<ElevatorStateDTO> fetchElevatorState(ElevatorInfo elevator) {
        try {
            String url = elevator.getBaseUrl() + "/api/elevator/" + elevator.getElevatorId() + "/status";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), ElevatorStateDTO.class));
            }
        } catch (Exception ignored) {
            // Αν κάποιο ασανσέρ δεν τρέχει ακόμα, κρατάμε την τοπική κατάσταση στο Central
        }
        return Optional.empty();
    }
}
