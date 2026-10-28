package elevator;

import common.DoorState;
import common.ElevatorCommandDTO;
import common.ElevatorStateDTO;
import common.Direction;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;

public class ElevatorApiController {

    // Το ID του συγκεκριμένου elevator.
    // Αργότερα θα δίνεται από τη γραμμή εντολών.
    private static String elevatorId = "A";

    // Τρέχουσα κατάσταση του elevator.
    private static int currentFloor = 0;
    private static Direction direction = Direction.IDLE;
    private static DoorState doorState = DoorState.CLOSED;

    // Οι επόμενες στάσεις του elevator.
    private static final List<Integer> nextStops = new ArrayList<>();

    public static void main(String[] args) {

        // 1. Εκκίνηση του REST server στην πόρτα 7002.
        Javalin app = Javalin.create().start(7002);

        System.out.println(
                "Elevator " + elevatorId
                        + " Controller τρέχει στο http://localhost:7002");

        // 2. Endpoint λήψης εντολής από το Central.
        //
        // POST /api/elevator/A/stop
        //
        // Body:
        // {
        //     "elevatorId": "A",
        //     "targetFloor": 25
        // }
        app.post("/api/elevator/{id}/stop", ctx -> {

            String requestedElevatorId =
                    ctx.pathParam("id");

            // Ελέγχουμε ότι η εντολή αφορά το συγκεκριμένο elevator.
            if (!elevatorId.equals(requestedElevatorId)) {
                ctx.status(404)
                        .result("Το ασανσέρ δεν βρέθηκε.");
                return;
            }

            // Μετατρέπουμε το JSON request
            // σε ElevatorCommandDTO.
            ElevatorCommandDTO command =
                    ctx.bodyAsClass(ElevatorCommandDTO.class);

            // Ελέγχουμε ότι το ID μέσα στο JSON
            // συμφωνεί με το ID του elevator.
            if (command.getElevatorId() != null
                    && !elevatorId.equals(command.getElevatorId())) {

                ctx.status(400)
                        .result("Το elevatorId της εντολής δεν είναι σωστό.");
                return;
            }

            int targetFloor = command.getTargetFloor();

            System.out.println(
                    "Ελήφθη εντολή από το Central: Elevator "
                            + elevatorId
                            + " -> όροφος "
                            + targetFloor);

            // Υπολογίζουμε την κατεύθυνση.
            if (targetFloor > currentFloor) {
                direction = Direction.UP;
            } else if (targetFloor < currentFloor) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.IDLE;
            }

            // Προσθέτουμε τον όροφο στις επόμενες στάσεις.
            if (!nextStops.contains(targetFloor)
                    && targetFloor != currentFloor) {

                nextStops.add(targetFloor);
            }

            ctx.status(200)
                    .result(
                            "Η εντολή ελήφθη από το Elevator "
                                    + elevatorId);
        });

        // 3. Endpoint επιστροφής της κατάστασης του elevator.
        //
        // GET /api/elevator/A/status
        app.get("/api/elevator/{id}/status", ctx -> {

            String requestedElevatorId =
                    ctx.pathParam("id");

            if (!elevatorId.equals(requestedElevatorId)) {
                ctx.status(404)
                        .result("Το ασανσέρ δεν βρέθηκε.");
                return;
            }

            ElevatorStateDTO state =
                    new ElevatorStateDTO(
                            elevatorId,
                            currentFloor,
                            direction,
                            doorState,
                            new ArrayList<>(nextStops));

            ctx.json(state);
        });
    }
}
