package ui;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Διαδραστικό τερματικό ορόφου (Floor UI) για την αποστολή κλήσεων στο Central.
 */
@Command(
        name = "floor-ui",
        mixinStandardHelpOptions = true,
        description = "Πάνελ ορόφου Destination Dispatch για κλήση ανελκυστήρα."
)
public class FloorUI implements Callable<Integer> {

    private static final String DEFAULT_CENTRAL_URL = "http://localhost:7000";

    @Option(
            names = {"-f", "--floor"},
            description = "Όροφος εγκατάστασης του UI",
            defaultValue = "0"
    )
    private int installationFloor;

    @Option(
            names = {"-u", "--central-url"},
            description = "Βασική διεύθυνση URL του Central",
            defaultValue = DEFAULT_CENTRAL_URL
    )
    private String centralUrl = DEFAULT_CENTRAL_URL;

    private CentralRequestClient requestClient;

    /**
     * Προεπιλεγμένος constructor για εκκίνηση μέσω Picocli.
     */
    public FloorUI() {
        this.installationFloor = 0;
        this.centralUrl = DEFAULT_CENTRAL_URL;
    }

    /**
     * Constructor με dependency injection του REST client (ιδανικός για unit testing).
     *
     * @param installationFloor ο όροφος εγκατάστασης του UI
     * @param requestClient ο REST client επικοινωνίας με το Central
     */
    public FloorUI(int installationFloor, CentralRequestClient requestClient) {
        if (requestClient == null) {
            throw new IllegalArgumentException("Request client cannot be null.");
        }
        this.installationFloor = installationFloor;
        this.requestClient = requestClient;
        this.centralUrl = requestClient.getCentralBaseUrl();
    }

    /**
     * Εκκινεί την εφαρμογή γραμμής εντολών του FloorUI μέσω Picocli.
     *
     * @param args τα ορίσματα γραμμής εντολών (π.χ. --floor 3)
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new FloorUI()).execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    @Override
    public Integer call() {
        if (this.requestClient == null) {
            this.requestClient = new CentralRequestClient(centralUrl);
        }
        initializeAtFloor(installationFloor);
        System.out.println("Σύνδεση με Central: " + requestClient.getCentralBaseUrl());

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            runLoop(scanner);
        }
        return 0;
    }

    /**
     * Ορίζει τον όροφο εγκατάστασης του πάνελ και εμφανίζει σχετικό μήνυμα.
     *
     * @param floor ο όροφος εγκατάστασης
     */
    public void initializeAtFloor(int floor) {
        this.installationFloor = floor;
        System.out.println("Το UI βρίσκεται στον όροφο " + installationFloor);
    }

    /**
     * Επιστρέφει τον τρέχοντα όροφο εγκατάστασης του UI.
     *
     * @return ο όροφος εγκατάστασης
     */
    public int getInstallationFloor() {
        return installationFloor;
    }

    /**
     * Στέλνει αίτημα μετάβασης στον επιλεγμένο όροφο προορισμού μέσω του REST client.
     *
     * @param destinationFloor ο όροφος προορισμού
     * @return Optional με το αναγνωριστικό του ανελκυστήρα ή κενό αν απορριφθεί/αποτύχει
     */
    public Optional<String> inputDestinationFloor(int destinationFloor) {
        if (destinationFloor == installationFloor) {
            System.out.println("Βρίσκεσαι ήδη στον όροφο " + installationFloor + ".");
            return Optional.empty();
        }
        if (this.requestClient == null) {
            this.requestClient = new CentralRequestClient(centralUrl);
        }
        return requestClient.requestElevator(installationFloor, destinationFloor);
    }

    /**
     * Εμφανίζει στον επιβάτη τον ανελκυστήρα που του ανατέθηκε.
     *
     * @param elevatorId το αναγνωριστικό του ανελκυστήρα (π.χ. "A")
     */
    public void displayAssignedElevator(String elevatorId) {
        System.out.println("Χρησιμοποίησε τον ανελκυστήρα " + elevatorId);
    }

    /**
     * Ζητά και εμφανίζει την τρέχουσα κατάσταση όλων των ανελκυστήρων από το Central.
     */
    public void displayElevatorsStatus() {
        if (this.requestClient == null) {
            this.requestClient = new CentralRequestClient(centralUrl);
        }
        Optional<String> status = requestClient.fetchElevatorsStatus();
        if (status.isPresent()) {
            System.out.println("Κατάσταση στόλου: " + status.get());
        } else {
            System.out.println("Δεν ήταν δυνατή η ανάκτηση της κατάστασης των ανελκυστήρων.");
        }
    }

    /**
     * Εκτελεί τον βρόχο εξυπηρέτησης πολλαπλών επιβατών μέχρι την εντολή εξόδου.
     *
     * @param scanner η πηγή ανάγνωσης εισόδου του χρήστη
     */
    public void runLoop(Scanner scanner) {
        System.out.println("Εντολές: <αριθμός ορόφου>, 'status' για κατάσταση, 'exit' για έξοδο.");
        while (true) {
            System.out.print("Δώσε όροφο προορισμού: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            if ("exit".equalsIgnoreCase(input) || "q".equalsIgnoreCase(input)) {
                System.out.println("Τερματισμός FloorUI.");
                break;
            }
            if ("status".equalsIgnoreCase(input)) {
                displayElevatorsStatus();
                continue;
            }
            handleFloorInput(input);
        }
    }

    private void handleFloorInput(String input) {
        try {
            int destinationFloor = Integer.parseInt(input);
            System.out.println("Επέλεξες τον όροφο " + destinationFloor);
            inputDestinationFloor(destinationFloor).ifPresent(this::displayAssignedElevator);
        } catch (NumberFormatException e) {
            System.out.println("Μη έγκυρη είσοδος. Δώσε ακέραιο όροφο, 'status' ή 'exit'.");
        }
    }
}
