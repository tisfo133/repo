package central;
import common.*;

public interface CentralControllerInterface {
    // --- ΚΑΤΗΓΟΡΙΑ Α: ΕΙΣΕΡΧΟΜΕΝΕΣ ΕΝΤΟΛΕΣ (Τις καλούν άλλοι προς το central) ---
    void configureSystem(SystemConfig config);
    char requestElevator(RideRequest request);

    // --- ΚΑΤΗΓΟΡΙΑ Β: ΕΞΕΡΧΟΜΕΝΕΣ ΕΝΕΡΓΕΙΕΣ (Τις εκτελεί το central προς τα έξω) ---
    void dispatchCommandToElevator(char elevatorId, int targetFloor);
    void updateFloorDisplay(int floor, char assignedElevatorId, String message);
}