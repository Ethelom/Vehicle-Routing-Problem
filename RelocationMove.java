package dmst.mebede.group12.vrp;

public class RelocationMove {
    private int originNodePosition;
    private int targetRoutePosition;
    private int targetNodePosition;
    private double moveCost = Double.MAX_VALUE;
    private double maxRouteTime;
	public double getMaxRouteTime() {
		return maxRouteTime;
	}
	public void setMaxRouteTime(double maxRouteTime) {
		this.maxRouteTime = maxRouteTime;
	}
	public int getOriginNodePosition() {
		return originNodePosition;
	}
	public void setOriginNodePosition(int originNodePosition) {
		this.originNodePosition = originNodePosition;
	}
	public int getTargetRoutePosition() {
		return targetRoutePosition;
	}
	public void setTargetRoutePosition(int targetRoutePosition) {
		this.targetRoutePosition = targetRoutePosition;
	}
	public int getTargetNodePosition() {
		return targetNodePosition;
	}
	public void setTargetNodePosition(int targetNodePosition) {
		this.targetNodePosition = targetNodePosition;
	}
	public double getMoveCost() {
		return moveCost;
	}
	public void setMoveCost(double moveCost) {
		this.moveCost = moveCost;
	}
}
