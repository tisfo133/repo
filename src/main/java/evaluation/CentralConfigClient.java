package evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.SystemConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * REST client για την αποστολή ρυθμίσεων (SystemConfig) από το Evaluation στο Central.
 */
public class CentralConfigClient {

    private static final String DEFAULT_CENTRAL_URL = "http://localhost:7000";
    private static final String CONFIG_ENDPOINT = "/api/central/config";
    private static final int CONNECT_TIMEOUT_SECONDS = 3;
    private static final int HTTP_OK = 200;

    private final String centralBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Δημιουργεί client με την προεπιλεγμένη διεύθυνση του Central (θύρα 7000).
     */
    public CentralConfigClient() {
        this(DEFAULT_CENTRAL_URL);
    }

    /**
     * Δημιουργεί client με προσαρμοσμένη βασική διεύθυνση URL του Central.
     *
     * @param centralBaseUrl η βασική διεύθυνση του Central (π.χ. http://localhost:7000)
     */
    public CentralConfigClient(String centralBaseUrl) {
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
     * Αποστέλλει τις ρυθμίσεις κτιρίου στο Central μέσω HTTP POST.
     *
     * @param config οι ρυθμίσεις του συστήματος
     * @return true αν το Central απάντησε με HTTP 200, αλλιώς false
     */
    public boolean sendConfig(SystemConfig config) {
        if (config == null) {
            return false;
        }
        try {
            String jsonBody = objectMapper.writeValueAsString(config);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(centralBaseUrl + CONFIG_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == HTTP_OK;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[EVALUATION] Interrupted while sending config: " + e.getMessage());
            return false;
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[EVALUATION] Failed to send config to Central: " + e.getMessage());
            return false;
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
