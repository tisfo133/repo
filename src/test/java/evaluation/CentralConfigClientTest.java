package evaluation;

import com.sun.net.httpserver.HttpServer;
import common.SystemConfig;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CentralConfigClientTest {

    private HttpServer mockCentralServer;

    @AfterEach
    void tearDown() {
        if (mockCentralServer != null) {
            mockCentralServer.stop(0);
        }
    }

    @Test
    void shouldReturnTrueWhenCentralAcceptsConfig() throws Exception {
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();

        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/config", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            String body = new String(
                    exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            receivedBody.set(body);

            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralConfigClient client = new CentralConfigClient("http://localhost:" + port);
        SystemConfig config = new SystemConfig(0, 10, 4, 7000);

        boolean result = client.sendConfig(config);

        assertTrue(result);
        assertEquals("POST", receivedMethod.get());
        assertTrue(receivedBody.get().contains("\"minFloor\":0"));
        assertTrue(receivedBody.get().contains("\"maxFloor\":10"));
        assertTrue(receivedBody.get().contains("\"numElevators\":4"));
    }

    @Test
    void shouldReturnFalseWhenCentralRejectsConfig() throws Exception {
        mockCentralServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockCentralServer.createContext("/api/central/config", exchange -> {
            byte[] response = "Invalid config".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        mockCentralServer.start();

        int port = mockCentralServer.getAddress().getPort();
        CentralConfigClient client = new CentralConfigClient("http://localhost:" + port);
        SystemConfig config = new SystemConfig(0, 10, 4, 7000);

        assertFalse(client.sendConfig(config));
    }

    @Test
    void shouldReturnFalseWhenConfigIsNull() {
        CentralConfigClient client = new CentralConfigClient("http://localhost:7000/");
        assertEquals("http://localhost:7000", client.getCentralBaseUrl());
        assertFalse(client.sendConfig(null));
    }

    @Test
    void shouldReturnFalseGracefullyWhenCentralIsOffline() {
        CentralConfigClient client = new CentralConfigClient("http://localhost:9999");
        SystemConfig config = new SystemConfig(0, 10, 3, 7000);

        assertFalse(client.sendConfig(config));
    }

    @Test
    void shouldRejectBlankBaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> new CentralConfigClient("   "));
    }
}
