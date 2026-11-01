package elevator;
import common.Direction;
import common.DoorState;
/* Εξερχόμενη επαφή (Outbound REST Client Contract) για την επικοινωνία του συστήματοσ
ελέγχου ανελκυστήρα με το υλικό (κινητήρα, θύρεσ, οθόνη)*/
public interface HardwareBridgeClient {
    /*Endpoint: POST /api/hardware/motor/move 
    JSON Request: { "elevatorId": "A", "direction": "UP" }
    */
    void move(Direction direction);
    /*
    Endpoint: POST /api/hardware/motor/stop
    JSON Request: { "elevatorId": "A" }
    */
    void stop();
    /*
    Endpoint: POST /api/hardware/doors/open
    JSON Request: { "elevatorId": "A", "doorState": "OPEN" }
    */
    void openDoors();
    /*
    Endpoint: POST /api/hardware/doors/close
    JSON Request: { "elevatorId": "A", "doorState": "CLOSED" }
    */
    void closeDoors();
    /*
    Endpoint: PUT /api/hardware/display
    JSON Request: { "elevatorId": "A", "currentFloor": 3, "direction": "UP" }
    */
    void updateDisplay(int currentFloor, Direction direction);
    }
