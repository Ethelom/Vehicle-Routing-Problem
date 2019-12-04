package dmst.mebede.group12.vrp;

public class Solution {
	private Node node;
	private int pos;
	private double cost;
	private double trialCost;
	public double getTrialCost() {
		return trialCost;
	}
	public void setTrialCost(double trialCost) {
		this.trialCost = trialCost;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public Solution(Node node, int pos, double c, double t) {
		super();
		this.node = node;
		this.pos = pos;
		this.cost = c;
		this.trialCost = t;
	}
	

}
