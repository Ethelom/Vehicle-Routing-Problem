package thodisis;

public class SwapMove {
    private int sourceIndex, targetIndex;
    private double moveCostFrom, moveCostTo, moveCost;
    //double cap...
    private Route examinedRouteFrom, examinedRouteTo;


    public SwapMove() {

    }

    protected void applySwapMove() {
        if(examinedRouteFrom.equals(examinedRouteTo)) {
            Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
            Node f = examinedRouteTo.getRouteNodes().get(targetIndex);

            examinedRouteFrom.getRouteNodes().set(sourceIndex, f);
            examinedRouteTo.getRouteNodes().set(targetIndex, b);

            examinedRouteFrom.setTotalRouteTimeInHrs(examinedRouteFrom.getTotalRouteTimeInHrs() + moveCost);
        } else {
            Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
            Node f = examinedRouteTo.getRouteNodes().get(targetIndex);

            examinedRouteFrom.setTotalRouteTimeInHrs(examinedRouteFrom.getTotalRouteTimeInHrs() + moveCostFrom);
            examinedRouteTo.setTotalRouteTimeInHrs(examinedRouteTo.getTotalRouteTimeInHrs() + moveCostTo);

            //consider caps

            examinedRouteFrom.getRouteNodes().set(sourceIndex, f);
            examinedRouteTo.getRouteNodes().set(targetIndex, b);
        }

    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
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

    public double getMoveCost() {
        return moveCost;
    }

    public void setMoveCost(double moveCost) {
        this.moveCost = moveCost;
    }

    public Route getExaminedRouteFrom() {
        return examinedRouteFrom;
    }

    public void setExaminedRouteFrom(Route examinedRouteFrom) {
        this.examinedRouteFrom = examinedRouteFrom;
    }

    public Route getExaminedRouteTo() {
        return examinedRouteTo;
    }

    public void setExaminedRouteTo(Route examinedRouteTo) {
        this.examinedRouteTo = examinedRouteTo;
    }

}
