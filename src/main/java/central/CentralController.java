package central;

import common.DestinationCallDTO;
import common.ElevatorStateDTO;
import common.SystemConfig;
import io.javalin.Javalin;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Κεντρικός REST Controller και σημείο εκκίνησης (CLI) του συστήματος Central Dispatch.
 */
public class CentralController implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "Θύρα ακρόασης Central", defaultValue = "7000")
    private int port;

    @Option(names = {"-e", "--elevators"}, description = "Πλήθος ανελκυστήρων (2-20)", defaultValue = "4")
    private int numElevators;

    @Option(names = {"--min-floor"}, description = "Κατώτερος όροφος", defaultValue = "0")
    private int minFloor;

    @Option(names = {"--max-floor"}, description = "Ανώτερος όροφος", defaultValue = "10")
    private int maxFloor;

    private final CentralService centralService;
    private final ElevatorRestClient elevatorClient;

    public CentralController() {
        this.centralService = new CentralService();
        this.elevatorClient = new ElevatorRestClient();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CentralController()).execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    @Override
    public Integer call() {
        SystemConfig initialConfig = new SystemConfig(minFloor, maxFloor, numElevators, port);
        centralService.configure(initialConfig);

        Javalin app = Javalin.create().start(port);
        System.out.println("Central Controller τρέχει στη θύρα " + port
                + " (" + numElevators + " ανελκυστήρες, όροφοι " + minFloor + ".." + maxFloor + ")");

        // 1. Δυναμική παραμετροποίηση από Evaluation / Admin
        app.post("/api/central/config", ctx -> {
            try {
                SystemConfig newConfig = ctx.bodyAsClass(SystemConfig.class);
                centralService.configure(newConfig);
                System.out.println("Ελήφθησαν νέες ρυθμίσεις: όροφοι "
                        + newConfig.getMinFloor() + "-" + newConfig.getMaxFloor()
                        + ", " + newConfig.getNumElevators() + " ασανσέρ.");
                ctx.status(200).result("Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς στο Central.");
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Μη έγκυρες ρυθμίσεις: " + e.getMessage());
            }
        });

        // 2. Αίτημα μετακίνησης επιβάτη από FloorUI
        app.post("/api/central/request", ctx -> {
            DestinationCallDTO request = ctx.bodyAsClass(DestinationCallDTO.class);
            System.out.println("Νέα κλήση: Από όροφο " + request.getFromFloor()
                    + " προς " + request.getToFloor());

            try {
                // Συγχρονισμός κατάστασης από τους ενεργούς ανελκυστήρες πριν την επιλογή
                for (ElevatorInfo info : centralService.getAllElevators()) {
                    elevatorClient.fetchElevatorState(info)
                            .ifPresent(state -> centralService.updateElevatorState(info.getElevatorId(), state));
                }

                ElevatorInfo assigned = centralService.assignCall(request);
                String assignedId = assigned.getElevatorId();

                // Αποστολή στάσης παραλαβής (αν δεν είναι ήδη εκεί) και προορισμού
                if (assigned.getCurrentFloor() != request.getFromFloor()) {
                    elevatorClient.sendStopCommand(assigned, request.getFromFloor());
                }
                elevatorClient.sendStopCommand(assigned, request.getToFloor());

                ctx.status(200).result(assignedId);
            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            }
        });

        // 3. Ενημέρωση κατάστασης από ανελκυστήρα
        app.post("/api/central/elevators/{id}/status", ctx -> {
            String id = ctx.pathParam("id");
            ElevatorStateDTO state = ctx.bodyAsClass(ElevatorStateDTO.class);
            centralService.updateElevatorState(id, state);
            ctx.status(200).result("OK");
        });

        // 4. Προβολή κατάστασης όλων των ανελκυστήρων
        app.get("/api/central/elevators", ctx -> ctx.json(centralService.getAllElevators()));

        return 0;
    }
}