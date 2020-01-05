package theodisis;

public class SwapMove {
    private int sourceIndex, targetIndex;
    private double moveCostFrom, moveCostTo, moveCost;
    //double cap...
    private Route examinedRouteFrom, examinedRouteTo;
    private double newMaxRouteWithThisSwapMove = Double.MAX_VALUE;
    private int firstRoutePosition;
    private int firstNodePosition;

    private int secondRoutePosition;
    private int secondNodePosition;


    public int getFirstRoutePosition() {
		return firstRoutePosition;
	}

	public void setFirstRoutePosition(int firstRoutePosition) {
		this.firstRoutePosition = firstRoutePosition;
	}

	public int getFirstNodePosition() {
		return firstNodePosition;
	}

	public void setFirstNodePosition(int firstNodePosition) {
		this.firstNodePosition = firstNodePosition;
	}

	public int getSecondRoutePosition() {
		return secondRoutePosition;
	}

	public void setSecondRoutePosition(int secondRoutePosition) {
		this.secondRoutePosition = secondRoutePosition;
	}

	public int getSecondNodePosition() {
		return secondNodePosition;
	}

	public void setSecondNodePosition(int secondNodePosition) {
		this.secondNodePosition = secondNodePosition;
	}

	public double getNewMaxRouteWithThisSwapMove() {
		return newMaxRouteWithThisSwapMove;
	}

	public void setNewMaxRouteWithThisSwapMove(double newMaxRouteWithThisSwapMove) {
		this.newMaxRouteWithThisSwapMove = newMaxRouteWithThisSwapMove;
	}

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
