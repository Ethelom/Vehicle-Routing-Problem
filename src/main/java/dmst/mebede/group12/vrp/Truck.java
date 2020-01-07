package dmst.mebede.group12.vrp;

public class Truck {

    private int truckID, remainingCap;
    public static final int speedKMH = 35;
    private final int maxCap = 3000;


    public Truck() {
        this.truckID = VehicleRoutingProblem.truckCounter.incrementAndGet();
        this.remainingCap = maxCap;
    }

    public int getTruckID() {
        return truckID;
    }

    public void setTruckID(int truckID) {
        this.truckID = truckID;
    }

    public int getRemainingCap() {
        return remainingCap;
    }

    public void setRemainingCap(int remainingCap) {
        this.remainingCap = remainingCap;
    }

    public int getMaxCap() {
        return maxCap;
    }

}

