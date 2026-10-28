package central;

import common.SystemConfig;
import common.RideRequest;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CentralController {

    // Αποθηκεύουμε τις ρυθμίσεις του κτηρίου όταν μας τις στείλει το evaluation
    private static SystemConfig currentConfig;
    
    // Το εργαλείο για να στέλνουμε εμείς εντολές στα ασανσέρ. einai kataskevasths
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        
        // 1. Εκκίνηση του Server στην πόρτα 7000
        Javalin app = Javalin.create().start(7000);
        System.out.println("Ο Central Controller τρέχει στο http://localhost:7000");

        // 2. Endpoint Λήψης Ρυθμίσεων (από το Evaluation)
        app.post("/api/central/config", ctx -> {
            // Το Javalin μετατρέπει αυτόματα το JSON σε αντικείμενο Java
            currentConfig = ctx.bodyAsClass(SystemConfig.class);
            System.out.println("Ελήφθησαν ρυθμίσεις: " + currentConfig.getTotalFloors() + " όροφοι.");
            ctx.status(200).result("Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς στο Central.");
        });

        // 3. Endpoint Λήψης Κλήσης Ασανσέρ (από το UI)
        app.post("/api/central/request", ctx -> {
            RideRequest request = ctx.bodyAsClass(RideRequest.class);
            System.out.println("Νέα κλήση: Από όροφο " + request.getSourceFloor() + 
                               " προς " + request.getDestinationFloor());

            // TODO: Εδώ θα μπει αργότερα ο αλγόριθμος επιλογής του καλύτερου ασανσέρ.
            // Προς το παρόν, επιλέγουμε το ασανσέρ 'A' για δοκιμή.
            char assignedElevator = 'A';

            // Στέλνουμε την εντολή στο ασανσέρ - πρώτα για τον όροφο που βρίσκεται, μετά για τον όροφο προορισμού.
            sendElevatorCommand(assignedElevator, request.getSourceFloor());
            sendElevatorCommand(assignedElevator, request.getDestinationFloor());

            // Επιστρέφουμε τον χαρακτήρα του ασανσέρ πίσω στην οθόνη (UI)
            ctx.status(200).result(String.valueOf(assignedElevator));
        });
    }

    /**
     * Βοηθητική μέθοδος για να στέλνει το Central εντολές στο REST API των ασανσέρ.
     */
    private static void sendElevatorCommand(char elevatorId, int targetFloor) {
        try {
            // Υποθέτουμε ότι η ομάδα του elevator άνοιξε τον server της στην πόρτα 7002
            String url = "http://localhost:7002/api/elevator/" + elevatorId + "/stop";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(targetFloor)))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Δόθηκε εντολή στο ασανσέρ " + elevatorId + " να πάει στον όροφο " + targetFloor);
            
        } catch (Exception e) {
            System.out.println("Σφάλμα επικοινωνίας με το ασανσέρ " + elevatorId + ": " + e.getMessage());
        }
    }
}