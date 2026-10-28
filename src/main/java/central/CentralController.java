package central;

import common.DestinationCallDTO;
import common.SystemConfig;
import io.javalin.Javalin;

public class CentralController {

    // Αποθηκεύουμε τις ρυθμίσεις του κτηρίου
    // όταν μας τις στείλει το Evaluation.
    private static SystemConfig currentConfig;

    public static void main(String[] args) {

        // 1. Εκκίνηση του REST server στην πόρτα 7000.
        Javalin app = Javalin.create().start(7000);

        System.out.println(
                "Ο Central Controller τρέχει στο http://localhost:7000");

        // 2. Endpoint λήψης ρυθμίσεων από το Evaluation.
        app.post("/api/central/config", ctx -> {

            currentConfig = ctx.bodyAsClass(SystemConfig.class);

            System.out.println(
                    "Ελήφθησαν ρυθμίσεις: από όροφο "
                            + currentConfig.getMinFloor()
                            + " έως όροφο "
                            + currentConfig.getMaxFloor()
                            + ", "
                            + currentConfig.getNumElevators()
                            + " ασανσέρ.");

            ctx.status(200)
                    .result("Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς στο Central.");
        });

        // 3. Endpoint λήψης κλήσης από το UI.
        app.post("/api/central/request", ctx -> {

            DestinationCallDTO request =
                    ctx.bodyAsClass(DestinationCallDTO.class);

            System.out.println(
                    "Νέα κλήση: Από όροφο "
                            + request.getFromFloor()
                            + " προς "
                            + request.getToFloor());

            // Ελέγχουμε ότι οι όροφοι είναι έγκυροι,
            // εφόσον έχει ήδη δοθεί configuration.
            if (currentConfig != null) {

                if (!currentConfig.isFloorValid(request.getFromFloor())
                        || !currentConfig.isFloorValid(request.getToFloor())) {

                    ctx.status(400)
                            .result("Μη έγκυρος όροφος.");
                    return;
                }
            }

            // TODO:
            // Εδώ θα μπει αργότερα ο αλγόριθμος επιλογής
            // του καλύτερου ασανσέρ.
            //
            // Προς το παρόν επιλέγουμε το ασανσέρ A για δοκιμή.
            String assignedElevator = "A";

            // Επιστρέφουμε το ID του ασανσέρ στο UI.
            ctx.status(200)
                    .result(assignedElevator);
        });
    }
}