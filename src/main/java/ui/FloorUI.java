package ui;
import java.util.Scanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FloorUI {
    private int installationFloor;

    
public void initializeAtFloor(int installationFloor) {
    this.installationFloor = installationFloor;
    System.out.println("Το UI βρίσκεται στον όροφο " + installationFloor);
}

    
public char inputDestinationFloor(int destinationFloor) {
    try {
        String json = "{\"fromFloor\":" + installationFloor
                + ",\"toFloor\":" + destinationFloor + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/api/central/request"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Σφάλμα Central: " + response.body());
            return '?';
        }

        return response.body().trim().charAt(0);

    } catch (Exception e) {
        System.out.println("Αδυναμία επικοινωνίας με το Central: " + e.getMessage());
        return '?';
    }
 }
    
    
    public void displayAssignedElevator( char elevatorId) {
        System.out.println( "Χρησιμοποίησε τον ανελκυστήρα " + elevatorId );
    }

    public static void main( String[] args ) {
        int installationFloor = Integer.parseInt( args[0] );
        FloorUI ui = new FloorUI();
        ui.initializeAtFloor( installationFloor );

        Scanner scanner = new Scanner( System.in );

        System.out.print( "Δώσε όροφο προορισμού: " );
        int destinationFloor = scanner.nextInt();

        System.out.println( "Επέλεξες τον όροφο " + destinationFloor );
        char elevator = ui.inputDestinationFloor(destinationFloor);

if (elevator != '?') {
    ui.displayAssignedElevator(elevator);
}
    }

}