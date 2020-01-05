package thodisis;


public class TwoOptMove {
	private int positionOfFirstRoute;
    private int positionOfSecondRoute;
    private int positionOfFirstNode;
    private int positionOfSecondNode;
    private double moveCost;
    private double newMaxRouteWithThisTwoOptMove;
	public double getNewMaxRouteWithThisTwoOptMove() {
		return newMaxRouteWithThisTwoOptMove;
	}
	public void setNewMaxRouteWithThisTwoOptMove(double newMaxRouteWithTHisTwoOptMove) {
		this.newMaxRouteWithThisTwoOptMove = newMaxRouteWithTHisTwoOptMove;
	}
	public int getPositionOfFirstRoute() {
		return positionOfFirstRoute;
	}
	public void setPositionOfFirstRoute(int positionOfFirstRoute) {
		this.positionOfFirstRoute = positionOfFirstRoute;
	}
	public int getPositionOfSecondRoute() {
		return positionOfSecondRoute;
	}
	public void setPositionOfSecondRoute(int positionOfSecondRoute) {
		this.positionOfSecondRoute = positionOfSecondRoute;
	}
	public int getPositionOfFirstNode() {
		return positionOfFirstNode;
	}
	public void setPositionOfFirstNode(int positionOfFirstNode) {
		this.positionOfFirstNode = positionOfFirstNode;
	}
	public int getPositionOfSecondNode() {
		return positionOfSecondNode;
	}
	public void setPositionOfSecondNode(int positionOfSecondNode) {
		this.positionOfSecondNode = positionOfSecondNode;
	}
	public double getMoveCost() {
		return moveCost;
	}
	public void setMoveCost(double moveCost) {
		this.moveCost = moveCost;
	}
}