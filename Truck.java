package dmst.mebede.group12.vrp;

public class Truck {

    private int truckID, remainingCap;
    public static final int speedKMH = 35;
    private Node currentNode;

    public Truck(Node startingNode) {
        this.truckID = VehicleRoutingProblem.truckCounter.incrementAndGet();
        this.remainingCap = 3000;
        this.currentNode = startingNode;
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

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

}
