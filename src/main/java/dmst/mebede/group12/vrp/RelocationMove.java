package dmst.mebede.group12.vrp;

public class RelocationMove {

    private int sourcePosition, targetPosition;
    private Route fromRoute, toRoute;
    private double moveCostFrom, moveCostTo;
    private double moveCost;
    private int fromRemainingCap, toRemainingCap;

    public RelocationMove() {

    }

    protected static boolean capConstrainsAreViolated(Route targetRoute, int incomingDemand) {
        return targetRoute.getTruck().getRemainingCap() < incomingDemand;
    }

    protected void applyRelocationMove() {

        Node relocatedNode = fromRoute.getRouteNodes().get(sourcePosition);

        fromRoute.getRouteNodes().remove(sourcePosition);

        if(fromRoute.equals(toRoute)) {
            if(sourcePosition > targetPosition) {
                toRoute.getRouteNodes().add(targetPosition + 1, relocatedNode);
            } else {
                toRoute.getRouteNodes().add(targetPosition, relocatedNode);
            }

        } else {
            toRoute.getRouteNodes().add(targetPosition + 1, relocatedNode);
        }
        fromRoute.getTruck().setRemainingCap(fromRemainingCap);
        toRoute.getTruck().setRemainingCap(toRemainingCap);

    }

    protected boolean isCostImproving() {
        return moveCost < 0;
    }

    public int getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(int sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(int targetPosition) {
        this.targetPosition = targetPosition;
    }

    public double getMoveCost() {
        return moveCost;
    }

    public void setMoveCost(double moveCost) {
        this.moveCost = moveCost;
    }

    public Route getFromRoute() {
        return fromRoute;
    }

    public void setFromRoute(Route fromRoute) {
        this.fromRoute = fromRoute;
    }

    public Route getToRoute() {
        return toRoute;
    }

    public void setToRoute(Route toRoute) {
        this.toRoute = toRoute;
    }

    public double getMoveCostFrom() {
        return moveCostFrom;
    }

    public void setMoveCostFrom(double moveCostFrom) {
        this.moveCostFrom = moveCostFrom;
    }

    public double getMoveCostTo() {
        return moveCostTo;
    }

    public void setMoveCostTo(double moveCostTo) {
        this.moveCostTo = moveCostTo;
    }

    public int getFromRemainingCap() {
        return fromRemainingCap;
    }

    public void setFromRemainingCap(int fromRemainingCap) {
        this.fromRemainingCap = fromRemainingCap;
    }

    public int getToRemainingCap() {
        return toRemainingCap;
    }

    public void setToRemainingCap(int toRemainingCap) {
        this.toRemainingCap = toRemainingCap;
    }

}

