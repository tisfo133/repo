package elevator;

import common.DoorState;
import common.Direction;
import common.ElevatorCommandDTO;
import common.ElevatorStateDTO;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;

public class ElevatorApiController {

    private static String elevatorId = "A";

    private static int currentFloor = 0;
    private static Direction direction = Direction.IDLE;
    private static DoorState doorState = DoorState.CLOSED;

    private static final List<Integer> nextStops = new ArrayList<>();

    public static Javalin createApp() {

        Javalin app = Javalin.create();

        app.post("/api/elevator/{id}/stop", ctx -> {

            String requestedElevatorId = ctx.pathParam("id");

            if (!elevatorId.equals(requestedElevatorId)) {
                ctx.status(404)
                        .result("Το ασανσέρ δεν βρέθηκε.");
                return;
            }

            ElevatorCommandDTO command =
                    ctx.bodyAsClass(ElevatorCommandDTO.class);

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

            if (targetFloor > currentFloor) {
                direction = Direction.UP;
            } else if (targetFloor < currentFloor) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.IDLE;
            }

            if (!nextStops.contains(targetFloor)
                    && targetFloor != currentFloor) {

                nextStops.add(targetFloor);
            }

            ctx.status(200)
                    .result(
                            "Η εντολή ελήφθη από το Elevator "
                                    + elevatorId);
        });

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

        return app;
    }

    public static void resetState() {
        currentFloor = 0;
        direction = Direction.IDLE;
        doorState = DoorState.CLOSED;
        nextStops.clear();
    }

    public static void main(String[] args) {

        Javalin app = createApp();

        app.start(7002);

        System.out.println(
                "Elevator " + elevatorId
                        + " Controller τρέχει στο http://localhost:7002");
    }
}
