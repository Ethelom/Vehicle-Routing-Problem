package dmst.mebede.group12.vrp;

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

    public VehicleRoutingProblem(int totalServicePoints, int totalTrucks)  {
        this.totalServicePoints = totalServicePoints;
        this.totalTrucks = totalTrucks;
        generateRandomNetwork();
        createTrucks();
        initializeRoutes();
        Utility utility = new Utility();
        this.timeMatrix = utility.convertDistanceToTime(distanceMatrix);
        applyNearestNeighborMethod();
        this.routes = vnd(this.routes, 3);
        this.routes = vns(cloneRoutes(this.routes));
        this.routes = simulatedAnnealing(cloneRoutes(this.routes));
        printFinalData();

        /*
        //validate our data

        for(Route r : routes) {
            double time = 0;
            for(int i = 0; i < r.getRouteSize() - 1; i++) {
                time += timeMatrix[r.getRouteNodes().get(i).getNodeID()][r.getRouteNodes().get(i+1).getNodeID()] + 0.25;

            }
            System.out.println("Route: " + r.getRouteID() + " has a total time of: " + time);
        }
        for(Route r : routes) {
            double load = 0;
            for(Node n : r.getRouteNodes()) {
                load += n.getDemand();
            }
            System.out.println("Route: " + r.getRouteID() + " has a load of: " + load + " remaining cap: " + r.getTruck().getRemainingCap());
        }
         */

    }

    private ArrayList<Route> simulatedAnnealing(ArrayList<Route> initial) {
        ArrayList<Route> bestSolution = initial;
        ArrayList<Route> currentSolution = initial;
        double temperature = 10000;
        double coolingRate = 0.003;
        int innerIterations = 40;
        do {
            for(int i = 0; i < innerIterations; i ++) {
                ArrayList<Route> sTonos = cloneRoutes(bestSolution);
                RandomMoveGenerator ranm = new RandomMoveGenerator(sTonos);
                for(int k = 0; k < 10; k++) {
                    SwapMove sm = ranm.getRandomSwapMove(timeMatrix);
                    sm.applySwapMove();
                    updateRouteCost(sm.getExaminedRouteFrom());
                    updateRouteCost(sm.getExaminedRouteTo());
                }
                if(accepted(currentSolution, sTonos, temperature)) {
                    currentSolution = cloneRoutes(sTonos);
                    if(objectiveFunctionIsImproved(bestSolution, currentSolution)) {
                        bestSolution = cloneRoutes(currentSolution);
                    }
                }
            }
            temperature *= (1 - coolingRate);
        } while(temperature > 1);
        return bestSolution;
    }

    private boolean accepted(ArrayList<Route> currentSolution,
        ArrayList<Route> sTonos, double temperature) {

        if(objectiveFunctionIsImproved(currentSolution, sTonos)) {
            return true;
        }

        double newCost = findSlowestRoute(sTonos).getTotalRouteTimeInHrs();
        double currentCost = findSlowestRoute(currentSolution).getTotalRouteTimeInHrs();

        if (newCost >= currentCost) {
            double power = -((newCost - currentCost) / temperature);
            double probThreshold = Math.exp(power);
            double p = Math.random();
            if(p <= probThreshold) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Route> vns(ArrayList<Route> initial) {
        ArrayList<Route> bestCurrentSolution = cloneRoutes(initial);
        long end = System.currentTimeMillis() + 110000;
        while(System.currentTimeMillis() < end) {
            int k = 1;
            while(k <= 2) {
                ArrayList<Route> sTonos = cloneRoutes(bestCurrentSolution);
                RandomMoveGenerator ranm = new RandomMoveGenerator(sTonos);
                if(k == 1) {
                    for(int i = 0; i < 1; i++) {
                        SwapMove sm = ranm.getRandomSwapMove(timeMatrix);
                        sm.applySwapMove();
                        updateRouteCost(sm.getExaminedRouteFrom());
                        updateRouteCost(sm.getExaminedRouteTo());
                    }
                } else if(k == 2) {
                    for(int i = 0; i < 1; i++) {
                        RelocationMove rm = ranm.getRandomRelocationMove(timeMatrix);
                        rm.applyRelocationMove();
                        updateRouteCost(rm.getFromRoute());
                        updateRouteCost(rm.getToRoute());
                    }
                }
                ArrayList<Route> s2 = vnd(sTonos, 3);
                if(objectiveFunctionIsImproved(bestCurrentSolution, s2)) {
                    bestCurrentSolution = cloneRoutes(s2);
                    k = 1;
                } else {
                    k += 1;
                }
            }
        }
        return bestCurrentSolution;
    }

    private ArrayList<Route> vnd(ArrayList<Route> initial, int kmax) {
        int k = 1;
        long end = System.currentTimeMillis() + 10000;
        while (k <= kmax && System.currentTimeMillis() < end) {
            ArrayList<Route> routes = cloneRoutes(initial);
            if (k == 3) {
                RelocationMove rm = findBestDecongestiveRelocationMove(routes);
                if(rm.getMoveCostFrom() < 0) {
                    rm.applyRelocationMove();
                    updateRouteCost(rm.getFromRoute());
                    updateRouteCost(rm.getToRoute());
                    initial = routes;
                    k = 1;
                } else {
                    k += 1;
                }
            } else if (k == 1) {
                SwapMove sm = findBestDecongestiveSwapMove(routes);
                if(sm.getMoveCostFrom() < 0) {
                    sm.applySwapMove();
                    updateRouteCost(sm.getExaminedRouteFrom());
                    updateRouteCost(sm.getExaminedRouteTo());
                    initial = routes;
                    k = 1;
                } else {
                    k += 1;
                }
            } else if(k == 2) {
                TwoOpt twoOpt = findBestTwoOptMove(routes);
                twoOpt.applyTwoOptMove();
                updateRouteCost(twoOpt.getFromRoute());
                updateRouteCost(twoOpt.getToRoute());
                if(objectiveFunctionIsImproved(initial, routes)) {
                    initial = routes;
                    k = 1;
                } else {
                    k += 1;
                }
            }
        }
        return initial;

    }

    private void updateRouteCost(Route route) {
        double timeCost = 0D;
        for(int i = 0; i < route.getRouteSize() - 1; i++) {
            Node a  = route.getRouteNodes().get(i);
            Node b = route.getRouteNodes().get(i + 1);
            timeCost += timeMatrix[a.getNodeID()][b.getNodeID()] + b.getServicetime();
        }
        route.setTotalRouteTimeInHrs(timeCost);

    }

    private boolean objectiveFunctionIsImproved(ArrayList<Route> scurrent, ArrayList<Route> snew) {
        return (findSlowestRoute(snew).getTotalRouteTimeInHrs() < findSlowestRoute(scurrent).getTotalRouteTimeInHrs());
    }

    private TwoOpt findBestTwoOptMove(ArrayList<Route> routes) {

        TwoOpt twoOpt = new TwoOpt();
        double bestMoveCost = Double.MAX_VALUE;

        for(int from = 0; from < routes.size(); from++) {
            Route fromRoute = routes.get(from);

            for(int to = 0; to < routes.size(); to++) {
                Route toRoute = routes.get(to);

                for(int fromIndex = 0; fromIndex < fromRoute.getRouteSize(); fromIndex++) {
                    int startTo = 0;
                    if(from == to) {
                        startTo = fromIndex + 2;
                    }

                    Node a = fromRoute.getRouteNodes().get(fromIndex);
                    Node b = null;
                    if(fromIndex + 1 < fromRoute.getRouteSize()) {
                        b = fromRoute.getRouteNodes().get(fromIndex + 1);
                    }

                    for(int toIndex = startTo; toIndex < toRoute.getRouteSize(); toIndex++) {
                        Node k = toRoute.getRouteNodes().get(toIndex);
                        Node l = null;
                        if(toIndex + 1 < toRoute.getRouteSize()) {
                            l = toRoute.getRouteNodes().get(toIndex + 1);
                        }

                        //start
                        double costAdded = 0D, costRemoved = 0D, moveCost = 0D;
                        if(from == to) {
                            if(fromIndex == 0 && toIndex == fromRoute.getRouteSize() - 1) {
                                continue;
                            }

                            if(fromIndex + 1 == toIndex) {
                                continue;
                            }

                            if( l != null) {
                                costAdded = timeMatrix[a.getNodeID()][k.getNodeID()] + timeMatrix[b.getNodeID()][l.getNodeID()];
                                costRemoved = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[k.getNodeID()][l.getNodeID()];
                            } else { //k is the last node of the route
                                costAdded = timeMatrix[a.getNodeID()][k.getNodeID()];
                                costRemoved = timeMatrix[a.getNodeID()][b.getNodeID()];
                            }


                        } else { //if routes are different
                            if(fromIndex == 0 && toIndex == 0) {
                                continue;
                            }

                            //if b and l are null
                            if(fromIndex == fromRoute.getRouteSize() - 1 && toIndex == toRoute.getRouteSize() - 1) {
                                continue;
                            }

                            if(capacityConstraintsAreViolated(fromRoute, fromIndex, toRoute, toIndex)) {
                                continue;
                            }

                            if(b != null && l != null) {
                                costAdded = timeMatrix[a.getNodeID()][l.getNodeID()] + timeMatrix[k.getNodeID()][b.getNodeID()];
                                costRemoved = timeMatrix[a.getNodeID()][b.getNodeID()] + timeMatrix[k.getNodeID()][l.getNodeID()];
                            } else if (b == null) {
                                costAdded = timeMatrix[a.getNodeID()][l.getNodeID()];
                                costRemoved = timeMatrix[k.getNodeID()][l.getNodeID()];
                            } else if (l == null) {
                                costAdded = timeMatrix[k.getNodeID()][b.getNodeID()];
                                costRemoved = timeMatrix[a.getNodeID()][b.getNodeID()];
                            }

                        }
                        moveCost = costAdded - costRemoved;
                        if(moveCost < bestMoveCost) {
                            bestMoveCost = moveCost;
                            twoOpt.setFromRoute(fromRoute);
                            twoOpt.setToRoute(toRoute);
                            twoOpt.setFromIndex(fromIndex);
                            twoOpt.setToIndex(toIndex);
                            twoOpt.setMoveCost(moveCost);
                        }
                    }

                }
            }
        }

        return twoOpt;
    }

    private boolean capacityConstraintsAreViolated(Route route1, int index1, Route route2, int index2) {
        double firstRouteFirstSegmentLoad = 0;
        for(int i = 0; i <= index1; i++) {
            firstRouteFirstSegmentLoad += route1.getRouteNodes().get(i).getDemand();
        }
        double firstRouteLoad = route1.getTruck().getMaxCap() - route1.getTruck().getRemainingCap();
        double firstRouteSecondSegmentLoad = firstRouteLoad - firstRouteFirstSegmentLoad;

        double secondRouteFirstSegmentLoad = 0;
        for(int i = 0; i <= index2; i++) {
            secondRouteFirstSegmentLoad += route2.getRouteNodes().get(i).getDemand();
        }
        double secondRouteLoad = route2.getTruck().getMaxCap() - route2.getTruck().getRemainingCap();
        double secondRouteSecondSegmentLoad = secondRouteLoad - secondRouteFirstSegmentLoad;

        if(firstRouteFirstSegmentLoad + secondRouteSecondSegmentLoad > route1.getTruck().getMaxCap()) {
            return true;
        }

        if(secondRouteFirstSegmentLoad + firstRouteSecondSegmentLoad > route2.getTruck().getMaxCap()) {
            return true;
        }

        return false;
    }

    private SwapMove findBestDecongestiveSwapMove(ArrayList<Route> routes) {
        SwapMove sm = new SwapMove();
        double bestMoveCostFrom = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        Route examinedRouteFrom = findSlowestRoute(routes);
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

                    //cap constraints
                    if(examinedRouteFrom.getTruck().getRemainingCap() + examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand() < examinedRouteTo.getRouteNodes().get(targetIndex).getDemand()) {
                        continue;
                    }

                    if(examinedRouteTo.getTruck().getRemainingCap() + examinedRouteTo.getRouteNodes().get(targetIndex).getDemand() < examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand()) {
                        continue;
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


                            int demandAbsoluteDifference = Math.abs(b.getDemand() - f.getDemand());
                            if(b.getDemand() == f.getDemand()) {
                                sm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap()); //ametablhto
                                sm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap());
                            } else if (b.getDemand() > f.getDemand()) {
                                sm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() + demandAbsoluteDifference);
                                sm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() - demandAbsoluteDifference);
                            } else {
                                sm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() - demandAbsoluteDifference);
                                sm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() + demandAbsoluteDifference);
                            }
                        }
                    }

                }
            }
        }

        return sm;
    }

    private ArrayList<Route> cloneRoutes(ArrayList<Route> routes2) {
        ArrayList<Route> clonedRoutes = new ArrayList<Route>();
        for(Route r : routes2) {
            Route clonedRoute = cloneRoute(r);
            clonedRoutes.add(clonedRoute);
        }
        return clonedRoutes;
    }

    private Route cloneRoute(Route r) {
        Truck clonedTruck = cloneTruck(r.getTruck());
        Route clonedRoute = new Route(clonedTruck);
        clonedRoute.setRouteID(r.getRouteID());
        clonedRoute.setFinalised(r.isFinalised());
        clonedRoute.setTotalRouteTimeInHrs(r.getTotalRouteTimeInHrs());
        clonedRoute.setRouteNodes(new ArrayList<Node>());
        for(Node node : r.getRouteNodes()) {
            clonedRoute.addNodeToRoute(node);
        }
        return clonedRoute;
    }

    private Truck cloneTruck(Truck truck) {
        Truck clonedTruck = new Truck();
        clonedTruck.setTruckID(truck.getTruckID());
        clonedTruck.setRemainingCap(truck.getRemainingCap());
        return clonedTruck;
    }

    private RelocationMove findBestDecongestiveRelocationMove(ArrayList<Route> routes) {
        RelocationMove rm = new RelocationMove();
        double bestMoveCostFrom = Double.MAX_VALUE;
        int totalRoutes = routes.size();

        Route examinedRouteFrom = findSlowestRoute(routes);
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
                                //checking if the route receiving the relocated node can accomodate its demand
                                relocatedDemand = examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand();
                                if(RelocationMove.capConstrainsAreViolated(examinedRouteTo, relocatedDemand)) {
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




    public Route findSlowestRoute(ArrayList<Route> routes) {
        return routes.stream().sorted(Comparator.comparingDouble(Route::getTotalRouteTimeInHrs)).collect(Collectors.toList()).get(routes.size() - 1);

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
            allTrucks.add(new Truck());
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

    private void applyNearestNeighborMethod() {
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

    public ArrayList<Route> findNQuickestRoutes(int n) {
        List<Route> availableRoutes = routes.stream().filter(t -> !t.isFinalised()).collect(Collectors.toList());
        List<Route> sortedList = availableRoutes.stream().sorted(Comparator.comparingDouble(Route::getTotalRouteTimeInHrs)).collect(Collectors.toList());
        if (sortedList.size() >= n) {
            return new ArrayList<Route>(sortedList.subList(0, n));
        } else {
            return new ArrayList<Route>(sortedList);
        }
    }

    private Pair<Route, Node> findBestAddition(ArrayList<Route> nQuickestRoutes, ArrayList<Node> potentialNodes) {
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