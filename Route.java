package dmst.mebede.group12.vrp;

import java.util.ArrayList;

public class Route {

    private int routeID;
    private Truck truck;
    private ArrayList<Node> routeNodes;
    private double totalRouteTimeInHrs;
    private boolean finalised;

    public Route(Truck truck) {
        this.truck = truck;
        this.routeID = truck.getTruckID();
        this.routeNodes = new ArrayList<Node>();
        this.totalRouteTimeInHrs = 0;
        this.finalised = false;
    }

    public void addNodeToRoute(Node node, int position) {
        routeNodes.add(position + 1, node);
        //TODO increase totalRouteTimeInHrs
    }
    
    public void addNodeToRoute(Node node) {
        routeNodes.add(node);
        //TODO increase totalRouteTimeInHrs
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public ArrayList<Node> getRouteNodes() {
        return routeNodes;
    }

    public void setRouteNodes(ArrayList<Node> routeNodes) {
        this.routeNodes = routeNodes;
    }

    public double getTotalRouteTimeInHrs() {
        return totalRouteTimeInHrs;
    }

    public void setTotalRouteTimeInHrs(double totalRouteTimeInHrs) {
        this.totalRouteTimeInHrs = totalRouteTimeInHrs;
    }

    public boolean isFinalised() {
        return finalised;
    }

    public void setFinalised(boolean finalised) {
        this.finalised = finalised;
    }
}