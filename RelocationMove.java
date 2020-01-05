package theodisis;

public class RelocationMove {

    private int sourcePosition, targetPosition;
    private Route fromRoute, toRoute;
    private double moveCostFrom, moveCostTo;
    private double moveCost;
    private int fromRemainingCap, toRemainingCap;
    private double maxRouteTime;
    
    private int originNodePosition;
    private int originRoutePosition;
    private int targetRoutePosition;
    private int targetNodePosition;

    public int getOriginNodePosition() {
		return originNodePosition;
	}

	public void setOriginNodePosition(int originNodePosition) {
		this.originNodePosition = originNodePosition;
	}

	public int getOriginRoutePosition() {
		return originRoutePosition;
	}

	public void setOriginRoutePosition(int originRoutePosition) {
		this.originRoutePosition = originRoutePosition;
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

	public double getMaxRouteTime() {
		return maxRouteTime;
	}

	public void setMaxRouteTime(double maxRouteTime) {
		this.maxRouteTime = maxRouteTime;
	}

	public RelocationMove() {

    }

    protected void applyRelocationMove() {
        //pairneis ton kombo apo thn sourcePosition kai ton bazeis sto diasthma targetPosition
        Node relocatedNode = fromRoute.getRouteNodes().get(sourcePosition);

        fromRoute.getRouteNodes().remove(sourcePosition);

        if(fromRoute.equals(toRoute)) {
            if(sourcePosition > targetPosition) {
                toRoute.getRouteNodes().add(targetPosition + 1, relocatedNode);
            } else {
                toRoute.getRouteNodes().add(targetPosition, relocatedNode);
            }
            toRoute.setTotalRouteTimeInHrs(toRoute.getTotalRouteTimeInHrs() + moveCost);
        } else {
            toRoute.getRouteNodes().add(targetPosition + 1, relocatedNode);
            fromRoute.setTotalRouteTimeInHrs(fromRoute.getTotalRouteTimeInHrs() + moveCostFrom);
            toRoute.setTotalRouteTimeInHrs(toRoute.getTotalRouteTimeInHrs() + moveCostTo);
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