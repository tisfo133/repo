import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class EvaluationApp {

    public static void main(String[] args) throws IOException {
        int port = 7002; // Η θύρα που συμφωνήσατε για το Evaluation
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Προσαρμογή στο endpoint που έδωσε ο συνεργάτης σου
        server.createContext("/api/evaluation/metrics", new MetricsHandler());
        
        server.setExecutor(null); 
        server.start();
        System.out.println("[EVALUATION SYSTEM] Ο Server τρέχει στη διεύθυνση http://localhost:" + port + "/api/evaluation/metrics");
    }

    static class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String jsonInput = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                
                System.out.println("[EVALUATION LOG] Ελήφθησαν δεδομένα τηλεμετρίας: " + jsonInput);

                int passengers = 0;
                boolean isOverloaded = false;

                try {
                    // Εξάγουμε το πεδίο "passengersOnboard" από το JSON του συνεργάτη σου
                    int index = jsonInput.indexOf("passengersOnboard");
                    if (index != -1) {
                        int colonIndex = jsonInput.indexOf(":", index);
                        int commaIndex = jsonInput.indexOf(",", colonIndex);
                        if (commaIndex == -1) commaIndex = jsonInput.indexOf("}", colonIndex);
                        
                        String numStr = jsonInput.substring(colonIndex + 1, commaIndex).trim();
                        passengers = Integer.parseInt(numStr);
                        
                        if (passengers > 8) {
                            isOverloaded = true;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[EVALUATION ERROR] Σφάλμα στην ανάγνωση των επιβατών: " + e.getMessage());
                }

                String response;
                if (isOverloaded) {
                    System.out.println("[EVALUATION ALERT] ⚠️ ΠΑΡΑΒΑΣΗ ΧΩΡΗΤΙΚΟΤΗΤΑΣ: Υπάρχουν " + passengers + " άτομα (Όριο: 8)!");
                    response = "{\"status\": \"FAILED\", \"violation\": \"CAPACITY_EXCEEDED\", \"message\": \"Overload detected! Max limit is 8 passengers.\"}";
                } else {
                    // Διαβάζουμε την ενέργεια (action) για να καταγράψουμε τι κάνει ο ανελκυστήρας
                    if (jsonInput.contains("DOORS_CLOSED")) {
                        System.out.println("[EVALUATION LOG] 🚪 Οι πόρτες έκλεισαν. Επιβάτες: " + passengers);
                    } else if (jsonInput.contains("DOORS_OPEN")) {
                        System.out.println("[EVALUATION LOG] 🚪 Οι πόρτες άνοιξαν. Επιβάτες: " + passengers);
                    } else {
                        System.out.println("[EVALUATION LOG] ✅ Καταγράφηκε ενέργεια ανελκυστήρα με " + passengers + " επιβάτες.");
                    }
                    
                    response = "{\"status\": \"SUCCESS\", \"message\": \"Metrics recorded successfully.\"}";
                }
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}