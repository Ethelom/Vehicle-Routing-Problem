public class SwapMove {
    private int firstRoutePosition;
    private int firstNodePosition;

    private int secondRoutePosition;
    private int secondNodePosition;

    private double moveCost;
    private double newMaxRouteWithThisSwapMove = Double.MAX_VALUE;

	public double getNewMaxRouteWithThisSwapMove() {
		return newMaxRouteWithThisSwapMove;
	}

	public void setNewMaxRouteWithThisSwapMove(double newMaxRouteWithThisSwapMove) {
		this.newMaxRouteWithThisSwapMove = newMaxRouteWithThisSwapMove;
	}

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

	public double getMoveCost() {
		return moveCost;
	}

	public void setMoveCost(double moveCost) {
		this.moveCost = moveCost;
	}
    
    
        
    }