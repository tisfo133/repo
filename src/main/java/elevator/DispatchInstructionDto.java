package elevator;

public record DispatchInstructionDto(
        int destinationFloor,
        Direction direction
) {
}