package central;

import common.DestinationCallDTO;
import common.SystemConfig;
import io.javalin.Javalin;

public class CentralController {

    private static SystemConfig currentConfig;
    
    // ΕΠΑΝΑΦΟΡΑ: Το εργαλείο για να στέλνουμε εμείς εντολές στα ασανσέρ
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(7000);

        app.post("/api/central/config", ctx -> {
            currentConfig = ctx.bodyAsClass(SystemConfig.class);
            System.out.println("Ελήφθησαν ρυθμίσεις: από όροφο "
                    + currentConfig.getMinFloor() + " έως όροφο "
                    + currentConfig.getMaxFloor() + ", "
                    + currentConfig.getNumElevators() + " ασανσέρ.");
            ctx.status(200).result("Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς στο Central.");
        });

        app.post("/api/central/request", ctx -> {
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

            String assignedElevator = "A";

            // ΕΠΑΝΑΦΟΡΑ: Στέλνουμε την εντολή στο ασανσέρ
            sendElevatorCommand(assignedElevator, request.getFromFloor());
            sendElevatorCommand(assignedElevator, request.getToFloor());

            ctx.status(200).result(assignedElevator);
        });
    }

    // ΕΠΑΝΑΦΟΡΑ: Η βοηθητική μέθοδος αποστολής
    private static void sendElevatorCommand(String elevatorId, int targetFloor) {
        try {
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