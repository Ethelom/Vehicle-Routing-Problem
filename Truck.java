import java.util.ArrayList;

public class Truck {
	private int space = 3000;
	private int speed = 35;
	private ArrayList<Node> route;
	private ArrayList<Double> routeTimes;
	private int truckID;
	private double totalTime = 0;
	
	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public ArrayList<Node> getRoute() {
		return route;
	}

	public void setRoute(ArrayList<Node> route) {
		this.route = route;
	}

	public ArrayList<Double> getRouteTimes() {
		return routeTimes;
	}

	public void setRouteTimes(ArrayList<Double> routeTimes) {
		this.routeTimes = routeTimes;
	}

	public int getTruckID() {
		return truckID;
	}

	public void setTruckID(int truckID) {
		this.truckID = truckID;
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public Truck(int truckID) {
		 this.truckID = truckID;
	}
	 
	public void addToRoute(Node node, double distance) {
		route.add(node);
		routeTimes.add(distance / speed + node.getServiceTime() + totalTime);
		totalTime = totalTime + distance / speed + node.getServiceTime();
	}
	
	public boolean addToTruck(int demand) {
		if (this.space - demand >= 0) {
			this.space = this.space - demand;//we will have checked in main if enough space is available
			return true;
		}
		return false;
	}
}
