
public class Node {
	private int x;
	private int y;
	private int demand;
	private double serviceTime = 0.25;
	private int ID;
	private int belongsToTruck;
	private boolean isRouted = false;
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
	public double getServiceTime() {
		return serviceTime;
	}
	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getBelongsToTruck() {
		return belongsToTruck;
	}
	public void setBelongsToTruck(int belongsToTruck) {
		this.belongsToTruck = belongsToTruck;
	}
	public boolean isRouted() {
		return isRouted;
	}
	public void setRouted(boolean isRouted) {
		this.isRouted = isRouted;
	}
}
