package central;

import common.DestinationCallDTO;
import common.SystemConfig;
import io.javalin.Javalin;

// ΠΡΟΣΘΗΚΗ: Τα 4 απαραίτητα imports για την επικοινωνία δικτύου
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CentralController {

    // Οι ρυθμίσεις που έστειλε ο client. Παραμένουν null μέχρι να κληθεί το endpoint /config.
    private static SystemConfig currentConfig;
    
    // ΕΠΑΝΑΦΟΡΑ: Το εργαλείο για να στέλνουμε εμείς εντολές στα ασανσέρ
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(7000);

        // Αποθηκεύει τις ρυθμίσεις που λαμβάνει το Central σε μορφή JSON.
        app.post("/api/central/config", ctx -> {
            currentConfig = ctx.bodyAsClass(SystemConfig.class);
            System.out.println("Ελήφθησαν ρυθμίσεις: από όροφο "
                    + currentConfig.getMinFloor() + " έως όροφο "
                    + currentConfig.getMaxFloor() + ", "
                    + currentConfig.getNumElevators() + " ασανσέρ.");
            ctx.status(200).result("Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς στο Central.");
        });

        // Λαμβάνει αίτημα διαδρομής: όροφος παραλαβής (fromFloor) και προορισμός (toFloor).
        app.post("/api/central/request", ctx -> {
            // Το Javalin μετατρέπει το JSON του αιτήματος σε DestinationCallDTO.
            DestinationCallDTO request = ctx.bodyAsClass(DestinationCallDTO.class);
            System.out.println("Νέα κλήση: Από όροφο " + request.getFromFloor()
                    + " προς " + request.getToFloor());

            // Ο νέος έλεγχος εγκυρότητας ορόφων της ομάδας
            if (currentConfig != null) {
                if (!currentConfig.isFloorValid(request.getFromFloor())
                        || !currentConfig.isFloorValid(request.getToFloor())) {
                    ctx.status(400).result("Μη έγκυρος όροφος.");
                    return;
                }
            }

            // Προσωρινή ανάθεση: χρησιμοποιείται πάντα το ασανσέρ A.
            // TODO: Επιλογή κατάλληλου διαθέσιμου ασανσέρ με βάση την κατάστασή του
            // και το πλήθος ασανσέρ που δηλώνει το SystemConfig.
            String assignedElevator = "A";

            // ΕΠΑΝΑΦΟΡΑ: Στέλνουμε την εντολή στο ασανσέρ
            // Στέλνουμε πρώτα την παραλαβή και μετά τον προορισμό.
            // TODO: Να διασφαλιστεί ότι ο προορισμός δεν θα εκτελεστεί πριν την παραλαβή,
            // π.χ. με ενιαίο αίτημα διαδρομής ή με επιβεβαίωση άφιξης στον όροφο παραλαβής.
            sendElevatorCommand(assignedElevator, request.getFromFloor());
            sendElevatorCommand(assignedElevator, request.getToFloor());

            // Επιστρέφουμε το ID του ασανσέρ στο οποίο ανατέθηκε η κλήση.
            ctx.status(200).result(assignedElevator);
        });
    }

    // ΕΠΑΝΑΦΟΡΑ: Η βοηθητική μέθοδος αποστολής - ΤΩΡΑ ΜΕ JSON!
    private static void sendElevatorCommand(String elevatorId, int targetFloor) {
        try {
            String url = "http://localhost:7002/api/elevator/" + elevatorId + "/stop";
            
            // Φτιάχνουμε το JSON "με το χέρι" για να ταιριάζει ακριβώς
            // στο ElevatorCommandDTO που περιμένει η ομάδα του Elevator.
            // TODO: Να χρησιμοποιηθεί serializer αντί για χειροκίνητη δημιουργία JSON,
            // αν προστεθούν πεδία ή πιο σύνθετα δεδομένα στην εντολή.
            String jsonBody = "{\"elevatorId\":\"" + elevatorId + "\", \"targetFloor\":" + targetFloor + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    // ΠΡΟΣΘΗΚΗ: Λέμε στο Elevator ότι του στέλνουμε JSON
                    .header("Content-Type", "application/json")
                    // Στέλνουμε το JSON που μόλις φτιάξαμε
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // TODO: Έλεγχος του status code και αναφορά αποτυχίας στο endpoint /request.
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Δόθηκε εντολή στο ασανσέρ " + elevatorId + " να πάει στον όροφο " + targetFloor);
            
        } catch (Exception e) {
            // Καταγράφουμε αποτυχίες σύνδεσης ή αποστολής του HTTP αιτήματος.
            System.out.println("Σφάλμα επικοινωνίας με το ασανσέρ " + elevatorId + ": " + e.getMessage());
        }
    }