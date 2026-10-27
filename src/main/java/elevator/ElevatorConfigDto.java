package elevator;

public record ElevatorConfigDto(
        double maxSpeed,
        int doorOpenTime,
        int minFloor,
        int maxFloor
) {
}
