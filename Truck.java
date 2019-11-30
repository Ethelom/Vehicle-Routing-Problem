import java.util.ArrayList;

public class Truck {
	public int space = 3000;
	public int v = 35;
	public ArrayList route= new ArrayList();
	public int truckId;
	public Truck(int truckId) {
		this.truckId = truckId;
	}
	public boolean addToTruck(int demand) {
		boolean placement = true;
		if ((this.space-demand) >= 0) {
			this.space = this.space - demand;
		}else {
			placement = false;
		}
		return placement;
	}
}
