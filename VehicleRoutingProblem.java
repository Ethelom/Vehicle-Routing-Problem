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
    private double[][] distanceMatrix, timeMatrix;

    public VehicleRoutingProblem(int totalServicePoints, int totalTrucks) {
        this.totalServicePoints = totalServicePoints;
        this.totalTrucks = totalTrucks;
        generateRandomNetwork();
        createTrucks();
        initializeRoutes();
        Utility utility = new Utility();
        this.timeMatrix = utility.convertDistanceToTime(distanceMatrix);
        utility.printMatrix(distanceMatrix);
        applyAdvanced();
        localSearch();
        printFinalData();

        /*
        //validate our data
        for(Route r : routes) {
            double time = 0;
            for(int i = 0; i < r.getRouteSize() - 1; i++) {
                time += timeMatrix[r.getRouteNodes().get(i).getNodeID()][r.getRouteNodes().get(i+1).getNodeID()] + 0.25;
            }
            if(r.getTruck().getRemainingCap() > 3000) {
                System.out.println("paparia brhkame");
            }
            System.out.println("Route: " + r.getRouteID() + " has a total time of: " + time);
        }
         */

    }

    private void localSearch() {
        for(int ok = 0; ok < 10; ok++) {

            boolean localOptimumFound = false;
            do {
                RelocationMove rm = findBestRelocationMove();
                if(rm.isCostImproving()) {
                    rm.applyRelocationMove();
                } else {
                    localOptimumFound = true;
                }

            } while(!localOptimumFound);

            localOptimumFound = false;
            do {
                SwapMove sm = findBestDecongestiveSwapMove();
                if(sm.getMoveCostFrom() < 0) {
                    sm.applySwapMove();
                } else {
                    localOptimumFound = true;
                }
            } while(!localOptimumFound);

            localOptimumFound = false;
            do {
                RelocationMove rm = findBestDecongestiveRelocationMove();
                if(rm.getMoveCostFrom() < 0) {
                    rm.applyRelocationMove();
                } else {
                    localOptimumFound = true;
                }
            } while(!localOptimumFound);
            localOptimumFound = false;
            do {
            	
            } while(!localOptimumFound);
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

}