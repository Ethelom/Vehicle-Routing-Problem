package dmst.mebede.group12.vrp;

import java.util.Random;

public class Node {
    private int nodeID, x, y, demand;
    private double serviceTime;
    private boolean serviced;
    private final static Random ran = new Random(1);

    //depository Constructor
    public Node(int x, int y, int demand) {
        this.nodeID = VehicleRoutingProblem.nodeCounter.getAndIncrement();
        this.x = x;
        this.y = y;
        this.demand = demand;
        this.serviced = true;
    }

    public Node() {
        this.nodeID = VehicleRoutingProblem.nodeCounter.getAndIncrement();
        this.x = ran.nextInt(100);
        this.y = ran.nextInt(100);
        this.demand = 100 * (1 + ran.nextInt(5));
        this.serviceTime = 0.25;
        this.serviced = false;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public double getServicetime() {
        return serviceTime;
    }

    public boolean isServiced() {
        return serviced;
    }

    public void updateServiceStatus(boolean serviced) {
        this.serviced = serviced;
    }

    /*
    @Override
    public String toString() {
        return "ID is: " + this.nodeID + "\n"
            + "X is: " + this.x + "\n"
            + "Y is: " + this.y + "\n"
            + "Demand is: " + this.demand + "\n";
    }
     */

    @Override
    public String toString() {
        return String.valueOf(nodeID);
    }
}

