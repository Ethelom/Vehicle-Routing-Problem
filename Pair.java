package dmst.mebede.group12.vrp;

public class Pair<R, N> {
    private R route;
    private N node;
    private int positionInRoute;

    public Pair(R route, N node, int p) {
        this.route = route;
        this.node = node;
        this.positionInRoute = p;
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