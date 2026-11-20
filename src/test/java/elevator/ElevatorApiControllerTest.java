package elevator;

import common.Direction;
import common.ElevatorStateDTO;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElevatorApiControllerTest {

    private Javalin app;
    private String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        ElevatorApiController.resetState();

        app = ElevatorApiController.createApp();
        app.start(0);

        baseUrl = "http://localhost:"
                + app.jettyServer().server().getURI().getPort();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void correctElevatorShouldAcceptStopCommand() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/stop"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"elevatorId\":\"A\",\"targetFloor\":5}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void wrongElevatorIdShouldReturnNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/B/stop"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"elevatorId\":\"B\",\"targetFloor\":5}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void wrongElevatorIdInsideCommandShouldReturnBadRequest()
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/stop"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"elevatorId\":\"B\",\"targetFloor\":5}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void nullElevatorIdInsideCommandShouldBeAccepted()
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/stop"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"elevatorId\":null,\"targetFloor\":5}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void stopCommandShouldSetDirectionUp() throws Exception {
        sendStopCommand(5);

        ElevatorStateDTO state = getStatus();

        assertEquals(Direction.UP, state.getDirection());
    }

    @Test
    void stopCommandShouldSetDirectionDown() throws Exception {
        sendStopCommand(-2);

        ElevatorStateDTO state = getStatus();

        assertEquals(Direction.DOWN, state.getDirection());
    }

    @Test
    void stopCommandForCurrentFloorShouldSetIdle() throws Exception {
        sendStopCommand(0);

        ElevatorStateDTO state = getStatus();

        assertEquals(Direction.IDLE, state.getDirection());
    }

    @Test
    void stopCommandShouldAddDestinationToNextStops()
            throws Exception {

        sendStopCommand(5);

        ElevatorStateDTO state = getStatus();

        assertTrue(state.getNextStops().contains(5));
    }

    @Test
    void sameDestinationShouldNotBeAddedTwice() throws Exception {
        sendStopCommand(5);
        sendStopCommand(5);

        ElevatorStateDTO state = getStatus();

        assertEquals(1, state.getNextStops().size());
    }

    @Test
    void statusShouldReturn200ForCorrectElevator() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/status"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void statusShouldReturn404ForWrongElevator() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/B/status"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void initialStatusShouldHaveCorrectElevatorId() throws Exception {
        ElevatorStateDTO state = getStatus();

        assertEquals("A", state.getElevatorId());
    }

    @Test
    void initialStatusShouldHaveClosedDoors() throws Exception {
        ElevatorStateDTO state = getStatus();

        assertEquals(
                common.DoorState.CLOSED,
                state.getDoorState());
    }

    private void sendStopCommand(int targetFloor) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/stop"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"elevatorId\":\"A\",\"targetFloor\":"
                                + targetFloor + "}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    private ElevatorStateDTO getStatus() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/elevator/A/status"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(response.body(), ElevatorStateDTO.class);
    }
}
