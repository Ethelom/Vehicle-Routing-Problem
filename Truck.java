package thodisis;

public class Truck {

    private int truckID, remainingCap;
    public static final int speedKMH = 35;


    public Truck(Node startingNode) {
        this.truckID = VehicleRoutingProblem.truckCounter.incrementAndGet();
        this.remainingCap = 3000;
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

}

