package ui;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloorUITest {

    private HttpServer mockCentralServer;

    @AfterEach
    void tearDown() {
        if (mockCentralServer != null) {
            mockCentralServer.stop(0);
        }
    }

    @Test
    void shouldReturnAssignedElevatorForValidDestination() throws Exception {
        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/request", exchange -> {
            byte[] response = "A".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);
        FloorUI ui = new FloorUI(2, client);

        Optional<String> assigned = ui.inputDestinationFloor(8);

        assertEquals(Optional.of("A"), assigned);
    }

    @Test
    void shouldRejectSameFloorWithoutCallingCentral() {
        CentralRequestClient client = new CentralRequestClient("http://localhost:9999");
        FloorUI ui = new FloorUI(4, client);

        Optional<String> assigned = ui.inputDestinationFloor(4);

        assertTrue(assigned.isEmpty());
    }

    @Test
    void shouldHandleMultipleInputsAndCommandsInLoop() throws Exception {
        AtomicInteger requestCalls = new AtomicInteger(0);
        AtomicInteger statusCalls = new AtomicInteger(0);

        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/request", exchange -> {
            requestCalls.incrementAndGet();
            byte[] response = "C".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.createContext("/api/central/elevators", exchange -> {
            statusCalls.incrementAndGet();
            byte[] response = "[{\"elevatorId\":\"C\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);
        FloorUI ui = new FloorUI(1, client);

        String simulatedInput = String.join("\n",
                "",
                "not-a-number",
                "1",
                "6",
                "status",
                "exit"
        );
        try (Scanner scanner = new Scanner(simulatedInput)) {
            ui.runLoop(scanner);
        }

        assertEquals(1, requestCalls.get());
        assertEquals(1, statusCalls.get());
    }

    @Test
    void shouldUpdateInstallationFloor() {
        CentralRequestClient client = new CentralRequestClient("http://localhost:7000");
        FloorUI ui = new FloorUI(0, client);

        ui.initializeAtFloor(5);

        assertEquals(5, ui.getInstallationFloor());
    }

    @Test
    void shouldRejectNullRequestClient() {
        assertThrows(IllegalArgumentException.class, () -> new FloorUI(0, null));
    }
}
