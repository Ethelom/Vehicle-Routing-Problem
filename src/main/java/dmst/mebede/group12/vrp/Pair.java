package dmst.mebede.group12.vrp;

public class Pair<R, N> {
    private R route;
    private N node;

    public Pair(R route, N node) {
        this.route = route;
        this.node = node;
    }

    public R getRoute() {
        return route;
    }

    public N getNode() {
        return node;
    }

}

