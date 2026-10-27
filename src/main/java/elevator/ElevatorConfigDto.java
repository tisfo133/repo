package main.java.elevator;
//Παράμετροι διαμόρφωσης λειτουργίας του ανελκυστήρα (όροφοι και χρόνοι σε m/s)
public record ΕlevatorConfigDto(
    int minFloor,
    int maxFloor,
    int floorTravelTimeMs,
    int doorOperationTimeMs
) {
}
