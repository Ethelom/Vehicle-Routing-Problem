package dmst.mebede.group12.vrp;

import java.util.ArrayList;
import java.util.Random;

public class RandomMoveGenerator {

    private ArrayList<Route> routes;

    public RandomMoveGenerator(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public SwapMove getRandomSwapMove(double[][] timeMatrix) {
        SwapMove sm;
        do {
            sm = findRandomSwapMove(timeMatrix);
        } while(sm == null);
        return sm;
    }

    public RelocationMove getRandomRelocationMove(double[][] timeMatrix) {
        RelocationMove rm;
        do {
            rm = findRandomRelocationMove(timeMatrix);
        } while(rm == null);
        return rm;
    }

    private RelocationMove findRandomRelocationMove(double[][] timeMatrix) {
        RelocationMove rm = new RelocationMove();
        int totalRoutes = routes.size();

        Route examinedRouteFrom;
        do {
            //in case it is trying to select an already empty route
            examinedRouteFrom = routes.get(new Random().nextInt(totalRoutes));
        } while(examinedRouteFrom.getRouteSize() < 1);
        Route examinedRouteTo = routes.get(new Random().nextInt(totalRoutes));

        int from = routes.indexOf(examinedRouteFrom);
        int to = routes.indexOf(examinedRouteTo);
        int examinedRouteFromSize = examinedRouteFrom.getRouteSize();
        int examinedRouteToSize = examinedRouteTo.getRouteSize();

        int sourceIndex = new Random().nextInt(examinedRouteFromSize - 1) + 1; //avoiding depository

        int targetIndex = new Random().nextInt(examinedRouteToSize - 1);

        Node a = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
        Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
        Node c = null;
        if(sourceIndex + 1 != examinedRouteFromSize) { //if you are not examining the last Node
            c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
        }

        if(to == from) {
            if(sourceIndex == targetIndex + 1 || sourceIndex == targetIndex) {
                return null;
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

        int relocatedDemand = 0;
        if(from != to) {
            //checking if the route receiving the relocated node can accomodate its demand
            relocatedDemand = examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand();
            if(RelocationMove.capConstrainsAreViolated(examinedRouteTo, relocatedDemand)) {
                return null;
            }
        }

        rm.setSourcePosition(sourceIndex);
        rm.setTargetPosition(targetIndex);
        rm.setFromRoute(examinedRouteFrom);
        rm.setToRoute(examinedRouteTo);
        rm.setMoveCostFrom(moveCostFrom);
        rm.setMoveCostTo(moveCostTo);
        rm.setMoveCost(moveCost);
        rm.setFromRemainingCap(examinedRouteFrom.getTruck().getRemainingCap() + relocatedDemand);
        rm.setToRemainingCap(examinedRouteTo.getTruck().getRemainingCap() - relocatedDemand);

        return rm;
    }


    private SwapMove findRandomSwapMove(double[][] timeMatrix) {
        SwapMove sm = new SwapMove();
        int totalRoutes = routes.size();

        Route examinedRouteFrom = routes.get(new Random().nextInt(totalRoutes));
        int from = routes.indexOf(examinedRouteFrom);

        Route examinedRouteTo = routes.get(new Random().nextInt(totalRoutes));
        int to = routes.indexOf(examinedRouteTo);

        if(examinedRouteTo.getRouteSize() == 0) {
            System.out.println(examinedRouteTo.getRouteNodes());
        }
        int sourceIndex = new Random().nextInt(examinedRouteFrom.getRouteSize() - 1) + 1;
        int targetIndex = new Random().nextInt(examinedRouteTo.getRouteSize() - 1) + 1;


        Node a  = examinedRouteFrom.getRouteNodes().get(sourceIndex - 1);
        Node b = examinedRouteFrom.getRouteNodes().get(sourceIndex);
        Node c = null;
        if(sourceIndex + 1 < examinedRouteFrom.getRouteSize()) { //if you are not examining the last Node
            c = examinedRouteFrom.getRouteNodes().get(sourceIndex + 1);
        }

        if(to == from) {
            if(sourceIndex == targetIndex) {
                return null;
            }
        }

        //cap constraints
        if(examinedRouteFrom.getTruck().getRemainingCap() + examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand() < examinedRouteTo.getRouteNodes().get(targetIndex).getDemand()) {
            return null;
        }

        if(examinedRouteTo.getTruck().getRemainingCap() + examinedRouteTo.getRouteNodes().get(targetIndex).getDemand() < examinedRouteFrom.getRouteNodes().get(sourceIndex).getDemand()) {
            return null;
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

        return sm;
    }
}
