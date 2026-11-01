package common;
//Συμβαν λειτουργίασ του ανελκυστήρα προς αποστολή στο σύστημα αξιολόγησης
//Παράδειγμα eventType : "FLOOR_REACHED", "DOORS_OPENED", "DOORS_CLOSED"
public record ElevatorEventDto(
    char elevatorId,
    long timestamp,
    String eventType,
    int floor
) {    
}
