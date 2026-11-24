package ui;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CentralRequestClientTest {

    private HttpServer mockCentralServer;

    @AfterEach
    void tearDown() {
        if (mockCentralServer != null) {
            mockCentralServer.stop(0);
        }
    }

    @Test
    void shouldReturnAssignedElevatorIdWhenCentralAcceptsRequest() throws Exception {
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();

        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/request", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            String body = new String(
                    exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            receivedBody.set(body);

            byte[] response = "B".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);

        Optional<String> assigned = client.requestElevator(2, 7);

        assertEquals(Optional.of("B"), assigned);
        assertEquals("POST", receivedMethod.get());
        assertTrue(receivedBody.get().contains("\"fromFloor\":2"));
        assertTrue(receivedBody.get().contains("\"toFloor\":7"));
    }

    @Test
    void shouldReturnEmptyWhenCentralRejectsRequest() throws Exception {
        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/request", exchange -> {
            byte[] response = "Invalid floor".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);

        assertTrue(client.requestElevator(0, 99).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenCentralReturnsBlankBody() throws Exception {
        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/request", exchange -> {
            byte[] response = "   ".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);

        assertTrue(client.requestElevator(1, 5).isEmpty());
    }

    @Test
    void shouldFetchElevatorsStatusWhenCentralRespondsOk() throws Exception {
        String sampleJson = "[{\"elevatorId\":\"A\",\"currentFloor\":0}]";

        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/elevators", exchange -> {
            byte[] response = sampleJson.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralRequestClient client = new CentralRequestClient("http://localhost:" + port);

        Optional<String> status = client.fetchElevatorsStatus();

        assertTrue(status.isPresent());
        assertEquals(sampleJson, status.get());
    }

    @Test
    void shouldHandleOfflineCentralGracefully() {
        CentralRequestClient client = new CentralRequestClient("http://localhost:9999/");
        assertEquals("http://localhost:9999", client.getCentralBaseUrl());

        assertTrue(client.requestElevator(0, 5).isEmpty());
        assertTrue(client.fetchElevatorsStatus().isEmpty());
    }

    @Test
    void shouldRejectBlankBaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> new CentralRequestClient("   "));
    }
}
