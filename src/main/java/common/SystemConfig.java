package common;

/**
 * Περιέχει τις βασικές παραμετροποιήσεις του κτηρίου και του συστήματος.
 */
public class SystemConfig {
    private int totalFloors;
    private int totalElevators;

    public SystemConfig(int totalFloors, int totalElevators) {
        this.totalFloors = totalFloors;
        this.totalElevators = totalElevators;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public int getTotalElevators() {
        return totalElevators;
    }
}
