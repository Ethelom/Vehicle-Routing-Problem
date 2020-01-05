public class Pair<R, N> {
    private R route;
    private N node;
    private int positionInRoute;
    private double cost;

    public Pair(R route, N node, int p, double cost) {
        this.route = route;
        this.node = node;
        this.positionInRoute = p;
        this.cost = cost;
    }

    public double getCost() {
		return cost;
	}
    

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getPositionInRoute() {
		return positionInRoute;
	}

	public void setPositionInRoute(int positionInRoute) {
		this.positionInRoute = positionInRoute;
	}

	public R getRoute() {
        return route;
    }

    public N getNode() {
        return node;
    }

}