package ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.DestinationCallDTO;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * REST client για την επικοινωνία του FloorUI με το Central Controller.
 */
public class CentralRequestClient {

    private static final String DEFAULT_CENTRAL_URL = "http://localhost:7000";
    private static final String REQUEST_ENDPOINT = "/api/central/request";
    private static final String ELEVATORS_ENDPOINT = "/api/central/elevators";
    private static final int CONNECT_TIMEOUT_SECONDS = 3;
    private static final int HTTP_OK = 200;

    private final String centralBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Δημιουργεί client με την προεπιλεγμένη διεύθυνση του Central (θύρα 7000).
     */
    public CentralRequestClient() {
        this(DEFAULT_CENTRAL_URL);
    }

    /**
     * Δημιουργεί client με προσαρμοσμένη βασική διεύθυνση URL του Central.
     *
     * @param centralBaseUrl η βασική διεύθυνση του Central (π.χ. http://localhost:7000)
     */
    public CentralRequestClient(String centralBaseUrl) {
        if (centralBaseUrl == null || centralBaseUrl.isBlank()) {
            throw new IllegalArgumentException("Central base URL cannot be null or blank.");
        }
        this.centralBaseUrl = centralBaseUrl.endsWith("/")
                ? centralBaseUrl.substring(0, centralBaseUrl.length() - 1)
                : centralBaseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Αποστέλλει αίτημα μετακίνησης επιβάτη στο Central και επιστρέφει το ID του ανελκυστήρα.
     *
     * @param fromFloor ο όροφος αφετηρίας
     * @param toFloor ο όροφος προορισμού
     * @return Optional με το αναγνωριστικό του ανελκυστήρα (π.χ. "A") ή κενό σε αποτυχία
     */
    public Optional<String> requestElevator(int fromFloor, int toFloor) {
        try {
            DestinationCallDTO call = new DestinationCallDTO(fromFloor, toFloor);
            String jsonBody = objectMapper.writeValueAsString(call);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(centralBaseUrl + REQUEST_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HTTP_OK) {
                System.out.println("[UI] Σφάλμα Central (" + response.statusCode() + "): "
                        + response.body());
                return Optional.empty();
            }

            String assignedId = response.body() == null ? "" : response.body().trim();
            if (assignedId.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(assignedId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[UI] Διακοπή κατά την αποστολή αιτήματος: " + e.getMessage());
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[UI] Αδυναμία επικοινωνίας με το Central: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Ανακτά την τρέχουσα κατάσταση όλων των ανελκυστήρων από το Central σε μορφή JSON.
     *
     * @return Optional με το JSON σώμα της απάντησης ή κενό αν αποτύχει η κλήση
     */
    public Optional<String> fetchElevatorsStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(centralBaseUrl + ELEVATORS_ENDPOINT))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK && response.body() != null) {
                return Optional.of(response.body());
            }
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[UI] Αδυναμία ανάκτησης κατάστασης στόλου: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Επιστρέφει τη βασική διεύθυνση URL του Central που χρησιμοποιεί ο client.
     *
     * @return το base URL του Central
     */
    public String getCentralBaseUrl() {
        return centralBaseUrl;
    }
}
