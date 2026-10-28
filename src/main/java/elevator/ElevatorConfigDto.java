package elevator;
//Παράμετροι διαμόρφωσης λειτουργίας του ανελκυστήρα (όροφοι και χρόνοι σε m/s)
public record ElevatorConfigDto(
    int minFloor,
    int maxFloor,
    int floorTravelTimeMs,
    int doorOperationTimeMs
) {
}
