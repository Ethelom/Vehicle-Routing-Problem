package thodisis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class VehicleRoutingProblem {
    protected static AtomicInteger truckCounter = new AtomicInteger(0);
    protected static AtomicInteger nodeCounter = new AtomicInteger(0);
    private int totalTrucks, totalServicePoints;
    private Node depository;
    private ArrayList<Node> allNodes;
    private ArrayList<Truck> allTrucks;
    private ArrayList<Route> routes;
    private double[][] distanceMatrix, timeMatrix, timeM;
    private Route maxRoute;

    public VehicleRoutingProblem(int totalServicePoints, int totalTrucks) {
        this.totalServicePoints = totalServicePoints;
        this.totalTrucks = totalTrucks;
        generateRandomNetwork();
        createTrucks();
        initializeRoutes();
        Utility utility = new Utility();
        this.timeMatrix = utility.convertDistanceToTime(distanceMatrix);
        //utility.printMatrix(distanceMatrix);
        applyAdvanced();
        calculateMaxRoute();
        
        //printFinalData();//τυπωνουμε
        CalculateTimeWithoutReturnToDepo();
        //utility.printMatrix(timeM);
        localSearch();
        
        //VND();
        printFinalData();

        
        //validate our data
        for(Route r : routes) {
            double time = 0;
            for(int i = 0; i < r.getRouteSize() - 1; i++) {
                time += timeMatrix[r.getRouteNodes().get(i).getNodeID()][r.getRouteNodes().get(i+1).getNodeID()] + 0.25;
            }
            if(r.getTruck().getRemainingCap() > 3000) {
                System.out.println("Error");
            }
            System.out.println("Route: " + r.getRouteID() + " has a total time of: " + time);
        }//ase aut

    }
    
    private void calculateMaxRoute()  {
		maxRoute = Collections.max(routes, Comparator.comparing(r -> r.getTotalRouteTimeInHrs()));
	}
    
    private void addDepositoryInEnd() {
  		for (int i = 0 ; i < routes.size(); i++) {
  			routes.get(i).getRouteNodes().add(depository);
  		}
  		
  	}
    
    private double CalculateMaxHoursInMove(double newRouteInHours, int originRouteIndex,
			int targetRouteIndex) {
		double maxHours = newRouteInHours;
    	for (int i = 0; i < routes.size(); i++) {
    			if ((i != originRouteIndex) && (i != targetRouteIndex)) {
    				if (routes.get(i).getTotalRouteTimeInHrs() > maxHours) {
    					maxHours = routes.get(i).getTotalRouteTimeInHrs();
    				}
    			}
    	}
		return maxHours;
	}
    
    private void CalculateTimeWithoutReturnToDepo() {
    	timeM = new double[totalServicePoints+1][totalServicePoints+1];//creation of a clone of timeMatrix
    	for (int i = 0; i < timeMatrix.length;i++) {
			for (int j = 0; j < timeMatrix.length; j++) {
				timeM[i][j] = 0;
			}
    	}
		for (int i = 0; i < timeMatrix.length;i++) {
			for (int j = 0; j < timeMatrix.length; j++) {
				if (j != 0) {
					timeM[i][j] = timeMatrix[i][j];
				}
				
			}
		}
	}
    
    private boolean findMove(int k) {
    	if (k ==1) {
    		RelocationMove rm = findBestRelocationMove();
    		if(rm.isCostImproving()) {
                rm.applyRelocationMove();
    		} else {
    			return true;
    		}
    	} else if (k ==2) {
    		SwapMove sm = findBestDecongestiveSwapMove();
    		if(sm.getMoveCostFrom() < 0) {
                sm.applySwapMove();
            } else {
            	return true;
            }
    	} else if (k ==3) {
    		RelocationMove rm = findBestDecongestiveRelocationMove();
            if(rm.getMoveCostFrom() < 0) {
                rm.applyRelocationMove();
            } else {
            	return true;
            }
    	} else if (k==4) {
    		TwoOptMove top = new TwoOptMove();
    		top.setMoveCost(Double.MAX_VALUE);
    		top.setNewMaxRouteWithThisTwoOptMove(Double.MAX_VALUE);
    		FindBestTwoOptMove(top);//
    		if ((top.getNewMaxRouteWithThisTwoOptMove() < maxRoute.getTotalRouteTimeInHrs()) || (top.getMoveCost() < - 0.0001)) {
    			ApplyTwoOptMove(top);
    		} else {
    			return true;
    		}
    	}
    	return false;
    }

    private void removeDepFromEnd() {
		for (Route r: routes) {
			r.getRouteNodes().remove(r.getRouteNodes().size()-1);
		}
		
	}

	private void localSearch() {
    	int k =1;
    	for (int ok=0; ok<100; ok++) {
            boolean localOptimumFound = false;
            if (k==4) {
            	addDepositoryInEnd();//προσθήκη αποθήκης στο τέλος
            }
            do { 
            	localOptimumFound = findMove(k);
            } while (!localOptimumFound);
            if (k ==4) {
            	removeDepFromEnd();
            }
            k++;
            if (k ==5) {
            	k = 1;
            }
            
        }
    }

    private SwapMove findBestDecongestiveSwapMove() {
        SwapMove sm = new SwapMove();
        double bestMoveCostFrom = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        Route examinedRouteFrom = findSlowestRoute();
        int from = routes.indexOf(examinedRouteFrom);
        double maxTime = examinedRouteFrom.getTotalRouteTimeInHrs();

        //iterate through every Route
        for(int to = 0; to < totalRoutes; to++) {
            Route examinedRouteTo = routes.get(to);

            for(int sourceIndex = 1; sourceIndex < routes.get(from).getRouteSize(); sourceIndex++) {
                Node a  = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
                Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
                Node c = null;
                if(sourceIndex + 1 < examinedRouteFrom.getRouteSize()) { //if you are not examining the last Node
                    c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
                }
                int secondIndex = 1;
                if(from == to) {
                    secondIndex = sourceIndex + 1;
                }
                for(int targetIndex = secondIndex; targetIndex < routes.get(to).getRouteSize(); targetIndex++) {

                    if(to == from) {
                        if(sourceIndex == targetIndex) {
                            continue;
                        }
                    }

                    Node e  = examinedRouteTo.getRouteNodes().get(targetIndex - 1);
                    Node f = examinedRouteTo.getRouteNodes().get(targetIndex);
                    Node g = null;

                    if(targetIndex + 1 != examinedRouteTo.getRouteSize()) { //if you are not examining the last Node
                        g = examinedRouteTo.getRouteNodes().get(targetIndex + 1);
                    }

                    double costRemoved1 = 0D, costAdded1 = 0D;
                    double costRemoved2 = 0D, costAdded2 = 0D;
                    double moveCostFrom = 0D, moveCostTo = 0D, moveCost = 0D;

                    if(from != to || (from == to && targetIndex != sourceIndex + 1)) {
                        if(c != null) {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][c.getNodeID()];
                            costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()] + timeMatrix[f.getNodeID()][c.getNodeID()];
                        } else {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()];
                            costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()];
                        }

                        if(g != null) {
                            costRemoved2 = timeMatrix[e.getNodeID()][f.getNodeID()] + timeMatrix[f.getNodeID()][g.getNodeID()];
                            costAdded2 = timeMatrix[e.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][g.getNodeID()];
                        } else {
                            costRemoved2 = timeMatrix[e.getNodeID()][f.getNodeID()];
                            costAdded2 = timeMatrix[e.getNodeID()][b.getNodeID()];
                        }


                    } else { //target = source + 1
                        if(targetIndex == sourceIndex + 1) {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()];
                            costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()];

                            if(g != null) {
                                costRemoved2 = timeMatrix[f.getNodeID()][g.getNodeID()];
                                costAdded2 = timeMatrix[b.getNodeID()][g.getNodeID()];
                            }

                        }
                    }

                    moveCostFrom = costAdded1 - costRemoved1;
                    moveCostTo = costAdded2 - costRemoved2;
                    moveCost = moveCostFrom + moveCostTo;

                    //decongestion
                    double criterion = Double.MAX_VALUE;
                    if(to == from) {
                        criterion = examinedRouteTo.getTotalRouteTimeInHrs() + moveCost;
                    } else {
                        criterion = examinedRouteTo.getTotalRouteTimeInHrs() + moveCostTo;
                    }

                    if(criterion < maxTime) {
                        if(moveCostFrom < bestMoveCostFrom && moveCost < 0D) {
                            bestMoveCostFrom = moveCostFrom;
                            sm.setSourceIndex(sourceIndex);
                            sm.setTargetIndex(targetIndex);
                            sm.setExaminedRouteFrom(examinedRouteFrom);
                            sm.setExaminedRouteTo(examinedRouteTo);
                            sm.setMoveCostFrom(moveCostFrom);
                            sm.setMoveCostTo(moveCostTo);
                            sm.setMoveCost(moveCost);
                        }
                    }

                }
            }
        }

        return sm;
    }


    /*DANGER ZONE START ☢ */
    private SwapMove findBestIntraSwapMove() {
        SwapMove sm = new SwapMove();
        double bestMoveCost = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        //iterate through every Route
        for(int i = 0; i < totalRoutes; i++) {

            Route examinedRoute = routes.get(i);

            for(int sourceIndex = 1; sourceIndex < routes.get(i).getRouteSize(); sourceIndex++) {

                Node a  = examinedRoute.getRouteNodes().get(sourceIndex - 1);
                Node b = examinedRoute.getRouteNodes().get(sourceIndex);
                Node c = null;
                if(sourceIndex + 1 != examinedRoute.getRouteSize()) { //if you are not examining the last Node
                    c = examinedRoute.getRouteNodes().get(sourceIndex + 1);
                }
                for(int targetIndex = sourceIndex + 1; targetIndex < routes.get(i).getRouteSize(); targetIndex++) {

                    if(sourceIndex == targetIndex) {
                        continue;
                    }

                    Node e  = examinedRoute.getRouteNodes().get(targetIndex - 1);
                    Node f = examinedRoute.getRouteNodes().get(targetIndex);
                    Node g = null;
                    if(targetIndex + 1 != examinedRoute.getRouteSize()) { //if you are not examining the last Node
                        g = examinedRoute.getRouteNodes().get(targetIndex + 1);
                    }

                    double costRemoved1 = 0D, costAdded1 = 0D;
                    double costRemoved2 = 0D, costAdded2 = 0D;
                    double moveCostFrom = 0D, moveCostTo = 0D, moveCost = 0D;

                    if(targetIndex != sourceIndex + 1) {
                        if(c != null) {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][c.getNodeID()];
                            costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()] + timeMatrix[f.getNodeID()][c.getNodeID()];
                        } else {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()];
                            costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()];
                        }

                        if(g != null) {
                            costRemoved2 = timeMatrix[e.getNodeID()][f.getNodeID()] + timeMatrix[f.getNodeID()][g.getNodeID()];
                            costAdded2 = timeMatrix[e.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][g.getNodeID()];
                        } else {
                            costRemoved2 = timeMatrix[e.getNodeID()][f.getNodeID()];
                            costAdded2 = timeMatrix[e.getNodeID()][b.getNodeID()];
                        }

                        moveCostFrom = costAdded1 - costRemoved1;
                        moveCostTo = costAdded2 - costRemoved2;
                        moveCost = moveCostFrom + moveCostTo;

                    } else { //target = source + 1
                        costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()];
                        costAdded1 = timeMatrix[a.getNodeID()][f.getNodeID()];

                        if(g != null) {
                            costRemoved2 = timeMatrix[f.getNodeID()][g.getNodeID()];
                            costAdded2 = timeMatrix[b.getNodeID()][g.getNodeID()];
                        }

                        moveCost = costAdded1 + costAdded2 - costRemoved1 - costRemoved2;
                    }


                    if(moveCost < bestMoveCost && moveCost < 0D) {
                        bestMoveCost = moveCost;
                        sm.setSourceIndex(sourceIndex);
                        sm.setTargetIndex(targetIndex);
                        //sm.setExaminedRoute(examinedRoute);
                        sm.setMoveCostFrom(moveCostFrom);
                        sm.setMoveCostTo(moveCostTo);
                        sm.setMoveCost(moveCost);
                    }

                }
            }
        }

        return sm;
    }
    /* DANGER ZONE END 😎 */
    private RelocationMove findBestDecongestiveRelocationMove() {
        RelocationMove rm = new RelocationMove();
        double bestMoveCostFrom = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        Route examinedRouteFrom = findSlowestRoute();
        int from = routes.indexOf(examinedRouteFrom);
        int examinedRouteFromSize = examinedRouteFrom.getRouteSize();
        double maxTime = examinedRouteFrom.getTotalRouteTimeInHrs();

        for(int to = 0; to < totalRoutes; to++) {
            Route examinedRouteTo = routes.get(to);

            for(int sourceIndex = 1; sourceIndex < routes.get(from).getRouteSize(); sourceIndex++) {
                Node a = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
                Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
                Node c = null;
                if(sourceIndex + 1 != examinedRouteFromSize) { //if you are not examining the last Node
                    c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
                }

                for(int targetIndex = 0; targetIndex < routes.get(to).getRouteSize() - 1; targetIndex++) { //συμβολίζει διαστήματα

                    //stupid relocation prevented
                    if(to == from) {
                        if(sourceIndex == targetIndex + 1 || sourceIndex == targetIndex) {
                            continue;
                        }
                    }

                    Node f = examinedRouteTo.getRouteNodes().get(targetIndex);
                    Node g = examinedRouteTo.getRouteNodes().get(targetIndex + 1);

                    //na mpei ektos for
                    double costAdded1, costRemoved1;
                    if(c != null) {
                        costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][c.getNodeID()] + b.getServicetime();
                        costAdded1 = timeMatrix[a.getNodeID()][c.getNodeID()];
                    } else {
                        costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + b.getServicetime(); //arc B-C does not exist, becuase B is the last Node
                        costAdded1 = 0;
                    }

                    double costRemoved2 = timeMatrix[f.getNodeID()][g.getNodeID()];
                    double costAdded2 = timeMatrix[f.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][g.getNodeID()] + b.getServicetime();

                    double moveCostFrom = costAdded1 - costRemoved1;
                    double moveCostTo = costAdded2 - costRemoved2;

                    double moveCost = moveCostFrom + moveCostTo;

                    double criterion = Double.MAX_VALUE;
                    if(to == from) {
                        criterion = examinedRouteTo.getTotalRouteTimeInHrs() + moveCost;
                    } else {
                        criterion = examinedRouteTo.getTotalRouteTimeInHrs() + moveCostTo;
                    }

                    if(criterion < maxTime) {
                        if (moveCostFrom < bestMoveCostFrom) {
                            int relocatedDemand = 0;
                            if(from != to) {
                                //na mpei pio panw
                                //checking if the route receiving the relocated node can accomodate its demand
                                relocatedDemand = examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand();
                                if(examinedRouteTo.getTruck().getRemainingCap() < relocatedDemand) {
                                    continue;
                                }
                            }

                            bestMoveCostFrom = moveCostFrom;
                            rm.setSourcePosition(sourceIndex);
                            rm.setTargetPosition(targetIndex);
                            rm.setFromRoute(examinedRouteFrom);
                            rm.setToRoute(examinedRouteTo);
                            rm.setMoveCostFrom(moveCostFrom);
                            rm.setMoveCostTo(moveCostTo);
                            rm.setMoveCost(moveCost);
                            rm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() + relocatedDemand);
                            rm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() - relocatedDemand);
                        }
                    }
                }
            }
        }
        return rm;
    }

    /*
    private RelocationMove findBestRelocationMoveFromMaxRoute() {
        RelocationMove rm = new RelocationMove();
        double bestMoveCost = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        Route examinedRouteFrom = findSlowestRoute();
        int from = routes.indexOf(examinedRouteFrom);
        int examinedRouteFromSize = examinedRouteFrom.getRouteSize();
        for(int to = 0; to < totalRoutes; to++) {
            Route examinedRouteTo = routes.get(to);

            for(int sourceIndex = 1; sourceIndex < routes.get(from).getRouteSize(); sourceIndex++) {
                Node a = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
                Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
                Node c = null;
                if(sourceIndex + 1 != examinedRouteFromSize) { //if you are not examining the last Node
                    c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
                }

                for(int targetIndex = 0; targetIndex < routes.get(to).getRouteSize() - 1; targetIndex++) { //συμβολίζει διαστήματα

                    if(to == from) {
                        if(sourceIndex == targetIndex + 1 || sourceIndex == targetIndex) {
                            continue;
                        }
                    }

                    Node f = examinedRouteTo.getRouteNodes().get(targetIndex);
                    Node g = examinedRouteTo.getRouteNodes().get(targetIndex + 1);

                    double costAdded1, costRemoved1;
                    if(c != null) {
                        costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][c.getNodeID()] + b.getServicetime();
                        costAdded1 = timeMatrix[a.getNodeID()][c.getNodeID()];
                    } else {
                        costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + b.getServicetime(); //arc B-C does not exist, becuase B is the last Node
                        costAdded1 = 0;
                    }

                    double costRemoved2 = timeMatrix[f.getNodeID()][g.getNodeID()];
                    double costAdded2 = timeMatrix[f.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][g.getNodeID()] + b.getServicetime();

                    double moveCostFrom = costAdded1 - costRemoved1;
                    double moveCostTo = costAdded2 - costRemoved2;

                    double moveCost = moveCostFrom + moveCostTo;

                    if (moveCost < bestMoveCost && moveCost < 0.00001) {
                        int relocatedDemand = 0;
                        if(from != to) {
                            //na mpei pio panw
                            //checking if the route receiving the relocated node can accomodate its demand
                            relocatedDemand = examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand();
                            if(examinedRouteTo.getTruck().getRemainingCap() < relocatedDemand) {
                                continue;
                            }
                        }

                        bestMoveCost = moveCost;
                        rm.setSourcePosition(sourceIndex);
                        rm.setTargetPosition(targetIndex);
                        rm.setFromRoute(examinedRouteFrom);
                        rm.setToRoute(examinedRouteTo);
                        rm.setMoveCostFrom(moveCostFrom);
                        rm.setMoveCostTo(moveCostTo);
                        rm.setMoveCost(moveCost);
                        rm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() + relocatedDemand);
                        rm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() - relocatedDemand);
                    }
                }
            }
        }

        return rm;
    }
     */

    private RelocationMove findBestRelocationMove() {
        RelocationMove rm = new RelocationMove();
        double bestMoveCost = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        for(int from = 0; from < totalRoutes; from++) {
            Route examinedRouteFrom = routes.get(from);
            int examinedRouteFromSize = examinedRouteFrom.getRouteSize();

            for(int to = 0; to < totalRoutes; to++) {
                Route examinedRouteTo = routes.get(to);

                if(routes.get(to).getTotalRouteTimeInHrs() >= routes.get(from).getTotalRouteTimeInHrs()) {
                    continue;
                }

                if(to == routes.indexOf(findSlowestRoute())) {
                    if(from != to) {
                        continue;
                    }
                }


                for(int sourceIndex = 1; sourceIndex < routes.get(from).getRouteSize(); sourceIndex++) {
                    Node a = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
                    Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
                    Node c = null;
                    if(sourceIndex + 1 != examinedRouteFromSize) { //if you are not examining the last Node
                        c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
                    }

                    for(int targetIndex = 0; targetIndex < routes.get(to).getRouteSize() - 1; targetIndex++) { //συμβολίζει διαστήματα

                        if(to == from) {
                            if(sourceIndex == targetIndex + 1 || sourceIndex == targetIndex) {
                                continue;
                            }
                        }

                        Node f = examinedRouteTo.getRouteNodes().get(targetIndex);
                        Node g = examinedRouteTo.getRouteNodes().get(targetIndex + 1);

                        double costAdded1, costRemoved1;
                        if(c != null) {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][c.getNodeID()] + b.getServicetime();
                            costAdded1 = timeMatrix[a.getNodeID()][c.getNodeID()];
                        } else {
                            costRemoved1 = timeMatrix[a.getNodeID()][b.getNodeID()] + b.getServicetime(); //arc B-C does not exist, becuase B is the last Node
                            costAdded1 = 0;
                        }

                        double costRemoved2 = timeMatrix[f.getNodeID()][g.getNodeID()];
                        double costAdded2 = timeMatrix[f.getNodeID()][b.getNodeID()] + timeMatrix[b.getNodeID()][g.getNodeID()] + b.getServicetime();

                        double moveCostFrom = costAdded1 - costRemoved1;
                        double moveCostTo = costAdded2 - costRemoved2;

                        double moveCost = moveCostFrom + moveCostTo;

                        if (moveCost < bestMoveCost) {
                            int relocatedDemand = 0;
                            if(from != to) {
                                //na mpei pio panw
                                //checking if the route receiving the relocated node can accomodate its demand
                                relocatedDemand = examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand();
                                if(examinedRouteTo.getTruck().getRemainingCap() < relocatedDemand) {
                                    continue;
                                }
                            }

                            bestMoveCost = moveCost;
                            rm.setSourcePosition(sourceIndex);
                            rm.setTargetPosition(targetIndex);
                            rm.setFromRoute(examinedRouteFrom);
                            rm.setToRoute(examinedRouteTo);
                            rm.setMoveCostFrom(moveCostFrom); //μεταβολή κόστους για την source route
                            rm.setMoveCostTo(moveCostTo);
                            rm.setMoveCost(moveCost);
                            rm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() + relocatedDemand); //storing the new remaining cap of each route
                            rm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() - relocatedDemand);
                        }
                    }
                }
            }
        }

        return rm;

    }


    private void printFinalData() {
        for(Route r : routes) {
            System.out.println("Route " + r.getRouteID() + ": ");
            System.out.println(r.getRouteNodes().toString());
            System.out.print("Route duration in hours: ");
            System.out.printf("%.2f", r.getTotalRouteTimeInHrs());
            System.out.println("\n");
        }

        Route maxRoute = Collections.max(routes, Comparator.comparing(r -> r.getTotalRouteTimeInHrs()));
        System.out.println("Longest route: " + maxRoute.getRouteID() + " (" + maxRoute.getTotalRouteTimeInHrs() + " hours)");

    }

    private void generateRandomNetwork() {
        createAllNodesAndServicePointLists();
        calculateEuclideanDistanceMatrix();
    }

    private void createAllNodesAndServicePointLists() {
        allNodes = new ArrayList<Node>();

        depository = new Node(50 , 50 , 0);
        allNodes.add(depository);

        for (int i = 0; i < totalServicePoints; i++) {
            Node customer = new Node();
            allNodes.add(customer);
        }
    }

    private void calculateEuclideanDistanceMatrix() {
        this.distanceMatrix = new double[allNodes.size()][allNodes.size()];
        for (int i = 0; i < allNodes.size(); i++) {
            Node from = allNodes.get(i);

            for (int j = 0; j < allNodes.size(); j++) {

                Node to = allNodes.get(j);

                double deltaX = (from.getX() - to.getX());
                double deltaY = (from.getY() - to.getY());
                double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

                distance = Math.round(distance);
                distanceMatrix[i][j] = distance;
            }
        }
    }

    private void createTrucks() {
        allTrucks = new ArrayList<Truck>();
        for(int i = 0; i < totalTrucks; i++) {
            allTrucks.add(new Truck(depository));
        }
    }

    private void initializeRoutes() {
        routes = new ArrayList<Route>(totalTrucks);

        for(Truck truck : allTrucks) {
            //create an initial Route for each truck
            Route route = new Route(truck);
            route.addNodeToRoute(depository);
            routes.add(route);
        }
    }

    private void applyAdvanced() {
        for (int i = 0; i < allNodes.size()-1; i++) {
            ArrayList<Node> potentialNodes;
            ArrayList<Route> nQuickestRoutes;
            do {
                nQuickestRoutes = findNQuickestRoutes(totalTrucks);
                potentialNodes = new ArrayList<>(nQuickestRoutes.size());
                for (Route r : nQuickestRoutes) {
                    potentialNodes.add(findMinUnservicedPoint(r));
                }
            } while(potentialNodes.contains(null));
            Pair<Route, Node> winningPair = findBestAddition(nQuickestRoutes, potentialNodes);
            Route minScoreRoute = winningPair.getRoute();
            Node nextNode = winningPair.getNode();
            double additionalCostHrs = timeMatrix[minScoreRoute.getCurrentNode().getNodeID()][nextNode.getNodeID()];
            minScoreRoute.addNodeToRoute(nextNode);
            minScoreRoute.updateTotalRouteTime(additionalCostHrs, nextNode);
            minScoreRoute.updateCap(nextNode);
        }
    }


    private Pair<Route, Node> findBestAddition(ArrayList<Route> nQuickestRoutes, ArrayList<Node> potentialNodes) {
        //List<Node> currentNodes = nQuickestRoutes.stream().map(Route::getTruck).map(Truck::getCurrentNode).collect(Collectors.toList());
        Route minScoreRoute = null;
        Node nextNode = null;

        double min = Double.MAX_VALUE;
        for(int j = 0; j < nQuickestRoutes.size(); j++) {
            Route r = nQuickestRoutes.get(j);
            int currNode = r.getCurrentNode().getNodeID();
            double score = r.getTotalRouteTimeInHrs() + timeMatrix[currNode][potentialNodes.get(j).getNodeID()];
            if (score < min) {
                min = score;
                minScoreRoute = r;
                nextNode = potentialNodes.get(j);
            }
        }
        return new Pair<Route, Node>(minScoreRoute, nextNode);
    }

    private Node findMinUnservicedPoint(Route currentQuickestRoute) {
        Truck truck = currentQuickestRoute.getTruck();
        int quickestRouteCurrentNode = currentQuickestRoute.getCurrentNode().getNodeID();
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for(int j = 1; j < allNodes.size(); j++) {
            if (!allNodes.get(j).isServiced() && allNodes.get(j).getDemand() <= truck.getRemainingCap()) {
                double candidate = timeMatrix[quickestRouteCurrentNode][j];
                if(candidate != 0D && candidate < min) {
                    min = candidate;
                    minIndex = j;
                }
            }
        }
        if (minIndex != -1) {
            return allNodes.get(minIndex);
        } else {
            currentQuickestRoute.setFinalised(true);
            return null;
        }
    }

    private ArrayList<Route> findNQuickestRoutes(int n) {
        List<Route> availableRoutes = routes.stream().filter(t -> !t.isFinalised()).collect(Collectors.toList());
        List<Route> sortedList = availableRoutes.stream().sorted(Comparator.comparingDouble(Route::getTotalRouteTimeInHrs)).collect(Collectors.toList());
        if (sortedList.size() >= n) {
            return new ArrayList<Route>(sortedList.subList(0, n));
        } else {
            return new ArrayList<Route>(sortedList);
        }
    }

    private Route findSlowestRoute() {
        return routes.stream().sorted(Comparator.comparingDouble(Route::getTotalRouteTimeInHrs)).collect(Collectors.toList()).get(routes.size() - 1);

    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new VehicleRoutingProblem(200, 25);
        long end = System.currentTimeMillis();
        float duration = (end - start) / 1000F;
        System.out.println();
        System.out.print("Algorithm execution duration in seconds: ");
        System.out.printf("%.3f", duration);
    }

    public int getTotalTrucks() {
        return totalTrucks;
    }

    public void setTotalTrucks(int totalTrucks) {
        this.totalTrucks = totalTrucks;
    }

    public int getTotalServicePoints() {
        return totalServicePoints;
    }

    public void setTotalServicePoints(int totalServicePoints) {
        this.totalServicePoints = totalServicePoints;
    }

    public ArrayList<Node> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(ArrayList<Node> allNodes) {
        this.allNodes = allNodes;
    }
    
	private void FindBestTwoOptMove(TwoOptMove top) {
        for (int rtInd1 = 0; rtInd1 < routes.size(); rtInd1++) {
            Route rt1 = routes.get(rtInd1);

            for (int rtInd2 = rtInd1; rtInd2 < routes.size(); rtInd2++) {
                Route rt2 = routes.get(rtInd2);

                for (int nodeInd1 = 0; nodeInd1 < rt1.getRouteNodes().size() - 1; nodeInd1++) {
                    int start2 = 0;
                    if (rt1 == rt2) {
                        start2 = nodeInd1 + 2;
                    }

                    for (int nodeInd2 = start2; nodeInd2 < rt2.getRouteNodes().size() - 1; nodeInd2++) 
                    {
                        double moveCost = Double.MAX_VALUE;
                        double newMaxRoute = 0; 
                        
                        if (rt1 == rt2) {
                            Node A = rt1.getRouteNodes().get(nodeInd1);
                            Node B = rt1.getRouteNodes().get(nodeInd1 + 1);
                            Node K = rt2.getRouteNodes().get(nodeInd2);
                            Node L = rt2.getRouteNodes().get(nodeInd2 + 1);

                            if (nodeInd1 == 0 && nodeInd2 == rt1.getRouteNodes().size() - 2) {
                                continue;
                            }

                            double costAdded = timeM[A.getNodeID()][K.getNodeID()] + timeM[B.getNodeID()][L.getNodeID()];
                            double costRemoved = timeM[A.getNodeID()][B.getNodeID()] + timeM[K.getNodeID()][L.getNodeID()];
                            newMaxRoute = rt1.getTotalRouteTimeInHrs() + costAdded - costRemoved;
                            moveCost =  costAdded - costRemoved;
                        } else {
                            Node A = (rt1.getRouteNodes().get(nodeInd1));
                            Node B = (rt1.getRouteNodes().get(nodeInd1 + 1));
                            Node K = (rt2.getRouteNodes().get(nodeInd2));
                            Node L = (rt2.getRouteNodes().get(nodeInd2 + 1));

                            if (nodeInd1 == 0 && nodeInd2 == 0) {
                                continue;
                            }
                            if (nodeInd1 == rt1.getRouteNodes().size() - 2 && nodeInd2 == rt2.getRouteNodes().size() - 2) {
                                continue;
                            }

                            if (CapacityConstraintsAreViolated(rt1, nodeInd1, rt2, nodeInd2)) {
                                continue;
                            }

                            double costAdded = timeM[A.getNodeID()][L.getNodeID()] + timeM[B.getNodeID()][K.getNodeID()];
                            double costRemoved = timeM[A.getNodeID()][B.getNodeID()] + timeM[K.getNodeID()][L.getNodeID()];
                            moveCost =  costAdded - costRemoved;
                            
                            ArrayList<Node>  newRoute1= new ArrayList<Node>();
                            ArrayList<Node>  newRoute2= new ArrayList<Node>();
                            for (int i = 0; i<= nodeInd1 ; i++) {
                            	newRoute1.add(rt1.getRouteNodes().get(i));
                            }
                            for (int i = nodeInd2 + 1 ; i < rt2.getRouteNodes().size(); i++)
                            {
                                newRoute1.add(rt2.getRouteNodes().get(i));
                            }
                            for (int i =0; i <= nodeInd2; i++) {
                            	newRoute2.add(rt2.getRouteNodes().get(i));
                            }
                            for (int i = nodeInd1 + 1 ; i < rt1.getRouteNodes().size(); i++)
                            {
                                newRoute2.add(rt1.getRouteNodes().get(i));
                            }
                            newMaxRoute = UpdateRouteTime(newRoute1);
                            double newMaxRoute2 = UpdateRouteTime(newRoute2);
                            if (newMaxRoute2 > newMaxRoute) {
                            	newMaxRoute = newMaxRoute2;
                            }
                       
                        }
                        newMaxRoute = CalculateMaxHoursInMove(newMaxRoute, rtInd1, rtInd2);//
                        if ((newMaxRoute < top.getNewMaxRouteWithThisTwoOptMove()) || (newMaxRoute == top.getNewMaxRouteWithThisTwoOptMove() && (moveCost < top.getMoveCost()) && (moveCost < -0.001))) 
                        {
                        	System.out.println("move cost in move two opt : "+ moveCost + " new time" + newMaxRoute);
                            StoreBestTwoOptMove(rtInd1, rtInd2, nodeInd1, nodeInd2, moveCost, top, newMaxRoute);
                        }
                    }
                }
            }
        }
        }
    
    private void ApplyTwoOptMove(TwoOptMove top) 
    {
    	System.out.println("inside applying two opt move");
        Route rt1 = routes.get(top.getPositionOfFirstRoute());
        Route rt2 = routes.get(top.getPositionOfSecondRoute());

        if (rt1 == rt2) 
        {	
        	System.out.println("inside same routes");
            ArrayList<Node> modifiedRt = new ArrayList<Node>();


            for (int i = 0; i <= top.getPositionOfFirstNode(); i++) 
            {
                modifiedRt.add(rt1.getRouteNodes().get(i));
            }
            for (int i = top.getPositionOfSecondNode(); i > top.getPositionOfFirstNode(); i--) 
            {
                modifiedRt.add(rt1.getRouteNodes().get(i));
            }
            for (int i = top.getPositionOfSecondNode() + 1; i < rt1.getRouteNodes().size(); i++) 
            {
                modifiedRt.add(rt1.getRouteNodes().get(i));
            }
            System.out.println("ready to set time & route nodes");
            System.out.println("Before : " +  rt1.getRouteNodes().toString());
            rt1.setRouteNodes(modifiedRt);
            System.out.println(modifiedRt.toString() +" After ");
            System.out.println("Route new time : " + (rt1.getTotalRouteTimeInHrs() + top.getMoveCost()));
            double newRouteTime = UpdateRouteTime(modifiedRt);
            rt1.setTotalRouteTimeInHrs(newRouteTime);
        }
        else
        {
        	System.out.println("inside different routes");
            ArrayList<Node> modifiedRt1 = new ArrayList<Node>();
            ArrayList<Node> modifiedRt2 = new ArrayList<Node>();
            
           
            for (int i = 0 ; i <= top.getPositionOfFirstNode(); i++)
            {
                modifiedRt1.add(rt1.getRouteNodes().get(i));
            }
             for (int i = top.getPositionOfSecondNode() + 1 ; i < rt2.getRouteNodes().size(); i++)
            {
                modifiedRt1.add(rt2.getRouteNodes().get(i));
            }
             
            for (int i = 0 ; i <= top.getPositionOfSecondNode(); i++)
            {
                modifiedRt2.add(rt2.getRouteNodes().get(i));
            }
            for (int i = top.getPositionOfFirstNode() + 1 ; i < rt1.getRouteNodes().size(); i++)
            {
                modifiedRt2.add(rt1.getRouteNodes().get(i));
            }
            
            int rt1SegmentLoad = 0;
            for (int i = 0 ; i <= top.getPositionOfFirstNode(); i++)
            {
                rt1SegmentLoad += rt1.getRouteNodes().get(i).getDemand();
            }
            
            int rt2SegmentLoad = 0;
            for (int i = 0 ; i <= top.getPositionOfSecondNode(); i++)
            {
                rt2SegmentLoad += rt2.getRouteNodes().get(i).getDemand();
            }
            //!!!!!!
            int originalRt1Load = 3000 - rt1.getTruck().getRemainingCap();
            int rt1Load =  rt1SegmentLoad + (3000 - rt2.getTruck().getRemainingCap() - rt2SegmentLoad);
            int rt2Load =  rt2SegmentLoad + (originalRt1Load - rt1SegmentLoad);
            
            rt1.getTruck().setRemainingCap(3000 - rt1Load);
            rt2.getTruck().setRemainingCap(3000 - rt2Load);
            
            rt1.setRouteNodes(modifiedRt1);
            rt2.setRouteNodes(modifiedRt2);
            double newTime1= UpdateRouteTime(modifiedRt1);
            double newTime2 = UpdateRouteTime(modifiedRt2);
            System.out.println("New route 1 time : " + newTime1 + "   New route 2 time : " + newTime2);
            System.out.println("Mod 1: " + modifiedRt1.toString()+ " Mod2 " + modifiedRt2.toString());
            
            rt1.setTotalRouteTimeInHrs(newTime1);
            rt2.setTotalRouteTimeInHrs(newTime2);
        }

    }
    
    private double UpdateRouteTime(ArrayList<Node> rt) 
    {
        double totCost = 0 ;
        for (int i = 0 ; i < rt.size()-2; i++)
        {
            Node A = rt.get(i);
            Node B = rt.get(i+1);
            totCost += timeM[A.getNodeID()][B.getNodeID()] +0.25;
        }
        return totCost;
    }
    
    private boolean CapacityConstraintsAreViolated(Route rt1, int nodeInd1, Route rt2, int nodeInd2) 
    {
        int rt1FirstSegmentLoad = 0;// μετράει πόσο έχει φορτωθεί το φορτηγό μέχρι το σημείο nodeInd1
        for (int i = 0 ; i <= nodeInd1; i++)
        {
            rt1FirstSegmentLoad += rt1.getRouteNodes().get(i).getDemand();
        }
        int rt1SecondSegment = rt1.getTruck().getRemainingCap() + rt1FirstSegmentLoad;
        
        int rt2FirstSegmentLoad = 0;
        for (int i = 0 ; i <= nodeInd2; i++)
        {
            rt2FirstSegmentLoad += rt2.getRouteNodes().get(i).getDemand();
        }
        int rt2SecondSegment = rt2.getTruck().getRemainingCap() + rt2FirstSegmentLoad;
        
        if (- rt1FirstSegmentLoad +  rt2SecondSegment > 0 )
        {
            return true;
        }
        
        if (- rt2FirstSegmentLoad +  rt1SecondSegment > 0)
        {
            return true;
        }
        
        return false;
    }

    private void StoreBestTwoOptMove(int rtInd1, int rtInd2, int nodeInd1, int nodeInd2, double moveCost, TwoOptMove top, double newMaxRouteWithThisTwoOptMove) {
        top.setPositionOfFirstRoute(rtInd1);
        top.setPositionOfSecondRoute(rtInd2);
        top.setPositionOfFirstNode(nodeInd1);
        top.setPositionOfSecondNode(nodeInd2);
        top.setNewMaxRouteWithThisTwoOptMove(newMaxRouteWithThisTwoOptMove);
        top.setMoveCost(moveCost);
    }

}