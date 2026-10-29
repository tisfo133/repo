package evaluation;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class EvaluationApp {

    // Μετρητές για τα στατιστικά αξιολόγησης της εκφώνησης
    private static int totalRequests = 0;
    private static int totalViolations = 0;

    public static void main(String[] args) throws IOException {
        int port = 7002; // Προεπιλεγμένη θύρα

        // Αν δοθεί όρισμα από τη γραμμή εντολών, το χρησιμοποιούμε ως θύρα
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("[EVALUATION WARNING] Μη έγκυρη θύρα στα ορίσματα. Χρήση προεπιλογής: 7002");
            }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Τυποποιημένη διεπαφή REST για τα metrics τηλεμετρίας
        server.createContext("/api/evaluation/metrics", new MetricsHandler());
        
        server.setExecutor(null); 
        server.start();
        System.out.println("[EVALUATION SYSTEM] Ο Server αξιολόγησης ξεκίνησε επιτυχώς στη θύρα: " + port);
    }

    static class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                totalRequests++;
                
                InputStream requestBody = exchange.getRequestBody();
                String jsonInput = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                
                System.out.println("[EVALUATION LOG] Αίτημα #" + totalRequests + " - Δεδομένα: " + jsonInput);

                int passengers = 0;
                boolean isOverloaded = false;

                try {
                    // Εξάγουμε το πεδίο "passengersOnboard" από το JSON
                    int index = jsonInput.indexOf("passengersOnboard");
                    if (index != -1) {
                        int colonIndex = jsonInput.indexOf(":", index);
                        int commaIndex = jsonInput.indexOf(",", colonIndex);
                        if (commaIndex == -1) commaIndex = jsonInput.indexOf("}", colonIndex);
                        
                        String numStr = jsonInput.substring(colonIndex + 1, commaIndex).trim();
                        passengers = Integer.parseInt(numStr);
                        
                        // Έλεγχος κανόνα ασφαλείας: μέγιστο όριο 8 επιβάτες
                        if (passengers > 8) {
                            isOverloaded = true;
                            totalViolations++;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[EVALUATION ERROR] Σφάλμα ανάλυσης δεδομένων: " + e.getMessage());
                }

                String response;
                if (isOverloaded) {
                    System.out.println("[EVALUATION ALERT] ⚠️ ΠΑΡΑΒΑΣΗ ΧΩΡΗΤΙΚΟΤΗΤΑΣ #" + totalViolations + ": Υπάρχουν " + passengers + " επιβάτες (Όριο: 8)!");
                    response = "{\"status\": \"FAILED\", \"violation\": \"CAPACITY_EXCEEDED\", \"message\": \"Overload detected! Max limit is 8 passengers.\"}";
                } else {
                    System.out.println("[EVALUATION LOG] ✅ Επιτυχής αξιολόγηση. Επιβάτες εντός ορίων: " + passengers);
                    response = "{\"status\": \"SUCCESS\", \"message\": \"Metrics recorded successfully. Total requests: " + totalRequests + "\"}";
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