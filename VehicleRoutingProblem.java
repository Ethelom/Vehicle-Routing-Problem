package dmst.mebede.group12.vrp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
    /**
    * Main method that calls everything.
    */
    public VehicleRoutingProblem(int totalServicePoints, int totalTrucks) {
        this.totalServicePoints = totalServicePoints;
        this.totalTrucks = totalTrucks;
        generateRandomNetwork();//randomly creates nodes
        createTrucks();//creation of trucks	
        initializeRoutes();
        convertDistanceToTime(distanceMatrix);//timeMatrix creation
        applyAdvanced();//
        addDepositoryInEnd();//προσθήκη αποθήκης στο τέλος
        printFinalData();//τυπωνουμε
        boolean endMove = false;//για να δουμε ποτε τελειωνει
        CalculateTimeWithoutReturnToDepo();//υπολογισμος χωρθς αποθηκη
        VND();
        //TabuSearch();
        calculateMaxRoute();
        System.out.println("Max hour : "  +maxRoute.getTotalRouteTimeInHrs());
        /*while (!endMove) {
        	RelocationMove rm = new RelocationMove();
        	applyBestRelocationMove(rm);
        	if (rm.getMoveCost() > -0.00001) {
        		endMove = true;s
        	} else {
        		ApplyRelocationMove(rm);
        		calculateMaxRoute();
        		System.out.println(" Relocation: " + maxRoute.getTotalRouteTimeInHrs()+ "route:" + maxRoute.getRouteNodes().toString());
            }
        }*/
    }
    
    private int DecideOperator() {
    	Random ran = new Random();
        return ran.nextInt(3);
        //return 0;
        //return 1;
    }
    
    private void VND() {
        
        // What would happen if Solution bestSolution = s;

        boolean terminationCondition = false;
        int localSearchIterator = 0;
        
        RelocationMove rm = new RelocationMove();
        SwapMove sm = new SwapMove();
        TwoOptMove top = new TwoOptMove();
        
        int k = 1;
        int kmax = 3;

        while (k <= kmax)
        {
            InitializeOperators(rm, sm, top);
            FindBestNeighbor(k, rm, sm, top);
            System.out.println("k is... :" + k);
             
            System.out.println("max route time: " +maxRoute.getTotalRouteTimeInHrs());
            if (MoveIsImproving(k, rm, sm, top))
            {
            	System.out.println("Apply move");
                ApplyMove(k, rm, sm, top);
                calculateMaxRoute();  
                System.out.println("set k=1");
                k = 1;
            }
            else
            {
            	System.out.println("Set k= "+ (k+1));
                k = k + 1;
            }
        }
       
    }
    
    private boolean MoveIsImproving(int k, RelocationMove rm, SwapMove sm, TwoOptMove top) 
    {
        if (k == 1)
        {
            if (rm.getMaxRouteTime() < maxRoute.getTotalRouteTimeInHrs())
            {
            	System.out.println("rm check" + rm.getMaxRouteTime() +"   max:"+ maxRoute.getTotalRouteTimeInHrs());
                return true;
            }
        }
        else if (k == 2)
        {
            if (sm.getNewMaxRouteWithThisSwapMove() <  maxRoute.getTotalRouteTimeInHrs())
            {
                return true;
            }
        }
        else if (k == 3)
        {
            if (top.getNewMaxRouteWithThisTwoOptMove() <  maxRoute.getTotalRouteTimeInHrs())
            {
                return true;
            }
        }
        
        return false;
    }

    
    private void FindBestNeighbor(int k, RelocationMove rm, SwapMove sm, TwoOptMove top) 
    {
        if (k == 1)
        {
           findBestRelocationMove(rm);
        }
        else if (k == 2)
        {
            FindBestSwapMove(sm);
        }
        else if (k == 3)
        {
            FindBestTwoOptMove(top); 
        }
    }
      
    
    private void TabuSearch() {

        RelocationMove rm = new RelocationMove();
        SwapMove sm = new SwapMove();
        TwoOptMove top = new TwoOptMove();
        
        for (int i = 0; i < 100; i++) {
            InitializeOperators(rm, sm, top);

            int operatorType = 3;//DecideOperator();
            System.out.println(operatorType);

            //Identify Best Move
            if (operatorType == 1) {
                findBestRelocationMove(rm);
            } else if (operatorType == 2) {
                FindBestSwapMove(sm);
            } else if (operatorType == 3) {
                FindBestTwoOptMove(top);
            }
            System.out.println("i has value" + i);
            if (LocalOptimumHasBeenReached(operatorType, rm, sm, top)) {
            	System.out.println("Time to break");
            	break;
            }

            //Apply move
            ApplyMove(operatorType, rm, sm, top);
            calculateMaxRoute();
            System.out.println("max route time: " + maxRoute.getTotalRouteTimeInHrs()+ "route:" + maxRoute.getRouteNodes().toString() + "top.movecost:" +top.getMoveCost());
        }
        printFinalData();
    }
    
    private boolean LocalOptimumHasBeenReached(int operatorType, RelocationMove rm, SwapMove sm, TwoOptMove top) {
        if (operatorType == 1) {
            if (rm.getMaxRouteTime() >= maxRoute.getTotalRouteTimeInHrs()) {
                return true;
            }
        } else if (operatorType == 2) {
            if (sm.getNewMaxRouteWithThisSwapMove() >= maxRoute.getTotalRouteTimeInHrs()) {
                return true;
            }
        }else if (operatorType == 3) {
            if (top.getNewMaxRouteWithThisTwoOptMove() >= maxRoute.getTotalRouteTimeInHrs()) {
                return true;
            }
        }

        return false;
    }
    
    private void FindBestSwapMove(SwapMove sm) {
        for (int firstRouteIndex = 0; firstRouteIndex < routes.size(); firstRouteIndex++) {
        	Route rt1 = routes.get(firstRouteIndex);
            for (int secondRouteIndex = firstRouteIndex; secondRouteIndex < routes.size(); secondRouteIndex++) {
                Route rt2 = routes.get(secondRouteIndex);
                for (int firstNodeIndex = 1; firstNodeIndex < rt1.getRouteNodes().size() - 1; firstNodeIndex++) {
                    int startOfSecondNodeIndex = 1;
                    if (rt1 == rt2) {
                        startOfSecondNodeIndex = firstNodeIndex + 1;
                    }
                    for (int secondNodeIndex = startOfSecondNodeIndex; secondNodeIndex < rt2.getRouteNodes().size() - 1; secondNodeIndex++) {
                    	Node a1 = rt1.getRouteNodes().get(firstNodeIndex - 1);
                        Node b1 = rt1.getRouteNodes().get(firstNodeIndex);
                        Node c1 = rt1.getRouteNodes().get(firstNodeIndex + 1);

                        Node a2 = rt2.getRouteNodes().get(secondNodeIndex - 1);
                        Node b2 = rt2.getRouteNodes().get(secondNodeIndex);
                        Node c2 = rt2.getRouteNodes().get(secondNodeIndex + 1);

                        double moveCost = Double.MAX_VALUE;
                        double newMaxRouteInHours = Double.MAX_VALUE;

                        if (rt1 == rt2) // within route 
                        {
                            if (firstNodeIndex == secondNodeIndex - 1) {
                                double costRemoved = timeM[a1.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][c2.getNodeID()];
                                double costAdded = timeM[a1.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][c2.getNodeID()];
                                moveCost = costAdded - costRemoved;                                                 
                            } else {
                                double costRemoved1 = timeM[a1.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][c1.getNodeID()];
                                double costAdded1 = timeM[a1.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][c1.getNodeID()];

                                double costRemoved2 = timeM[a2.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][c2.getNodeID()];
                                double costAdded2 = timeM[a2.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][c2.getNodeID()];

                                moveCost = costAdded1 + costAdded2 - (costRemoved1 + costRemoved2);
                            }
                            newMaxRouteInHours = rt1.getTotalRouteTimeInHrs() + moveCost;
                        } else 
                        {
                            if (- b1.getDemand() + b2.getDemand() > rt1.getTruck().getRemainingCap()) {
                                continue;
                            }
                            if (- b2.getDemand()+ b1.getDemand() > rt2.getTruck().getRemainingCap()) {
                                continue;
                            }

                            double costRemoved1 = timeM[a1.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][c1.getNodeID()];
                            double costAdded1 = timeM[a1.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][c1.getNodeID()];

                            double costRemoved2 = timeM[a2.getNodeID()][b2.getNodeID()] + timeM[b2.getNodeID()][c2.getNodeID()];
                            double costAdded2 = timeM[a2.getNodeID()][b1.getNodeID()] + timeM[b1.getNodeID()][c2.getNodeID()];
                            double costOfFirstTotalTime =  (rt1.getTotalRouteTimeInHrs() + costAdded1 - costRemoved1);
                            double costOfSecondTotalTime =  (rt2.getTotalRouteTimeInHrs() + costAdded2 - costRemoved2);
                            if (costOfFirstTotalTime >= costOfSecondTotalTime) {
                            	newMaxRouteInHours = costOfFirstTotalTime;
                            } else {
                            	newMaxRouteInHours = costOfSecondTotalTime;
                            }
                            moveCost = costAdded1 + costAdded2 - (costRemoved1 + costRemoved2);
                        }
                        newMaxRouteInHours = CalculateMaxHoursInMove(newMaxRouteInHours, firstRouteIndex, secondRouteIndex);
                        StoreBestSwapMove(firstRouteIndex, secondRouteIndex, firstNodeIndex, secondNodeIndex, moveCost, newMaxRouteInHours, sm);
                    }
                }
                
            }
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
    

    private void StoreBestSwapMove(int firstRouteIndex, int secondRouteIndex, int firstNodeIndex, int secondNodeIndex, double moveCost, double newMaxRouteInHours, SwapMove sm) {
        if (newMaxRouteInHours < sm.getNewMaxRouteWithThisSwapMove()) {
            sm.setFirstRoutePosition(firstRouteIndex);
            sm.setFirstNodePosition(firstNodeIndex);
            sm.setSecondRoutePosition(secondRouteIndex);
            sm.setSecondNodePosition(secondNodeIndex);
            sm.setMoveCost(moveCost);
            sm.setNewMaxRouteWithThisSwapMove(newMaxRouteInHours);
            System.out.println(sm.getNewMaxRouteWithThisSwapMove());
        }
    }
    
    
    private void ApplySwapMove(SwapMove sm) {
        if (sm.getMoveCost() == Double.MAX_VALUE) {
            return;
        }

        Route firstRoute = routes.get(sm.getFirstRoutePosition());
        Route secondRoute = routes.get(sm.getSecondRoutePosition());

        if (firstRoute == secondRoute) {
        	System.out.println("Equal Routes .Applying swap move");
        	if (sm.getFirstRoutePosition() == sm.getSecondRoutePosition() - 1) {
        		System.out.println("Next position");
                Node A = firstRoute.getRouteNodes().get(sm.getFirstNodePosition());
                Node B = firstRoute.getRouteNodes().get(sm.getFirstNodePosition() + 1);
                firstRoute.getRouteNodes().set(sm.getFirstNodePosition(), B);
                firstRoute.getRouteNodes().set(sm.getFirstNodePosition() + 1, A);

            } else {
                Node A = firstRoute.getRouteNodes().get(sm.getFirstNodePosition());
                Node B = firstRoute.getRouteNodes().get(sm.getSecondNodePosition());

                firstRoute.getRouteNodes().set(sm.getFirstNodePosition(), B);
                firstRoute.getRouteNodes().set(sm.getSecondNodePosition(), A);
            }
            firstRoute.setTotalRouteTimeInHrs(sm.getMoveCost());
        } else {
        	System.out.println("Not Equal Routes .Applying swap move");
            Node A = firstRoute.getRouteNodes().get(sm.getFirstNodePosition() - 1);
            Node B = firstRoute.getRouteNodes().get(sm.getFirstNodePosition());
            Node C = firstRoute.getRouteNodes().get(sm.getFirstNodePosition() + 1);

            Node E = secondRoute.getRouteNodes().get(sm.getSecondNodePosition() - 1);
            Node F = secondRoute.getRouteNodes().get(sm.getSecondNodePosition());
            Node G = secondRoute.getRouteNodes().get(sm.getSecondNodePosition() + 1);

            double costChangeFirstRoute = timeM[A.getNodeID()][F.getNodeID()] + timeM[F.getNodeID()][C.getNodeID()] - timeM[A.getNodeID()][B.getNodeID()] - timeM[B.getNodeID()][C.getNodeID()];
            double costChangeSecondRoute = timeM[E.getNodeID()][B.getNodeID()] + timeM[B.getNodeID()][G.getNodeID()] - timeM[E.getNodeID()][F.getNodeID()] - timeM[F.getNodeID()][G.getNodeID()];

            firstRoute.setTotalRouteTimeInHrs(costChangeFirstRoute);
            secondRoute.setTotalRouteTimeInHrs(costChangeSecondRoute);

            firstRoute.getTruck().setRemainingCap(F.getDemand() - B.getDemand());
            secondRoute.getTruck().setRemainingCap(B.getDemand() - F.getDemand());

            firstRoute.getRouteNodes().set(sm.getFirstNodePosition(), F);
            secondRoute.getRouteNodes().set(sm.getSecondNodePosition(), B);

        }
        
    }


	private void InitializeOperators(RelocationMove rm, SwapMove sm, TwoOptMove top) {
        rm.setMoveCost(Double.MAX_VALUE);
        sm.setMoveCost(Double.MAX_VALUE);
        top.setMoveCost(Double.MAX_VALUE);
    }
	
	private void ApplyMove(int operatorType, RelocationMove rm, SwapMove sm, TwoOptMove top) {
        if (operatorType == 1) {
            applyBestRelocationMove(rm);
        } else if (operatorType == 2) {
            ApplySwapMove(sm);
        }
        else if (operatorType == 3)
        {
            ApplyTwoOptMove(top);
        }
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
                            double costChangeFirst = timeM[A.getNodeID()][L.getNodeID()]  - timeM[A.getNodeID()][B.getNodeID()];
                            double costChangeSecond = timeM[B.getNodeID()][K.getNodeID()] - timeM[K.getNodeID()][L.getNodeID()];
                            newMaxRoute = rt1.getTotalRouteTimeInHrs() + costChangeFirst;
                            moveCost =  costAdded - costRemoved;
                            
                            if (newMaxRoute < (rt2.getTotalRouteTimeInHrs() + costChangeSecond)) {
                            	newMaxRoute = (rt2.getTotalRouteTimeInHrs() + costChangeSecond);
                            }
                       
                        }
                        newMaxRoute = CalculateMaxHoursInMove(newMaxRoute, rtInd1, rtInd2);
                        if (newMaxRoute < top.getNewMaxRouteWithThisTwoOptMove()) 
                        {
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
            rt1.setTotalRouteTimeInHrs(top.getMoveCost());
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
            
            rt1.setTotalRouteTimeInHrs(UpdateRouteCost(rt1));
            rt2.setTotalRouteTimeInHrs(UpdateRouteCost(rt2));
        }

    }
    
    private double UpdateRouteCost(Route rt) 
    {
        double totCost = 0 ;
        for (int i = 0 ; i < rt.getRouteNodes().size()-1; i++)
        {
            Node A = rt.getRouteNodes().get(i);
            Node B = rt.getRouteNodes().get(i+1);
            totCost += timeM[A.getNodeID()][B.getNodeID()];
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

	private void applyBestRelocationMove(RelocationMove rm) {
        if (rm.getMoveCost() == Double.MAX_VALUE) {
            return;
        }

        Route originRoute = routes.get(rm.getOriginRouteIndex());
        Route targetRoute =routes.get(rm.getTargetRoutePosition());

        Node B = originRoute.getRouteNodes().get(rm.getOriginNodePosition());

        if (originRoute == targetRoute) {
            originRoute.getRouteNodes().remove(rm.getOriginNodePosition());
            if (rm.getOriginNodePosition() < rm.getTargetNodePosition()) {
                targetRoute.getRouteNodes().add(rm.getTargetNodePosition(), B);
            } else {
                targetRoute.getRouteNodes().add(rm.getTargetNodePosition() + 1, B);
            }

            originRoute.setTotalRouteTimeInHrs(rm.getMoveCost());
        } else {
            Node A = originRoute.getRouteNodes().get(rm.getOriginNodePosition() - 1);
            Node C = originRoute.getRouteNodes().get(rm.getOriginNodePosition() + 1);

            Node F = targetRoute.getRouteNodes().get(rm.getTargetNodePosition());
            Node G = targetRoute.getRouteNodes().get(rm.getTargetNodePosition() + 1);

            double costChangeOrigin = timeM[A.getNodeID()][C.getNodeID()] - timeM[A.getNodeID()][B.getNodeID()] - timeM[B.getNodeID()][C.getNodeID()];
            double costChangeTarget = timeM[F.getNodeID()][B.getNodeID()] + timeM[B.getNodeID()][G.getNodeID()] - timeM[F.getNodeID()][G.getNodeID()];

            originRoute.getTruck().setRemainingCap( originRoute.getTruck().getRemainingCap() - B.getDemand());
            targetRoute.getTruck().setRemainingCap( originRoute.getTruck().getRemainingCap() + B.getDemand());

            originRoute.setTotalRouteTimeInHrs(costChangeOrigin);
            targetRoute.setTotalRouteTimeInHrs(costChangeTarget);

            originRoute.getRouteNodes().remove(rm.getOriginNodePosition());
            targetRoute.getRouteNodes().add(rm.getTargetNodePosition() + 1, B);

            double newMoveCost = costChangeOrigin + costChangeTarget;
            if (Math.abs(newMoveCost - rm.getMoveCost()) > 0.0001) {
                int problem = 0;
            }
        }
	}

    
    private void addDepositoryInEnd() {
		for (int i = 0 ; i < routes.size(); i++) {
			routes.get(i).getRouteNodes().add(depository);
		}
		
	}
    private void findBestRelocationMove(RelocationMove rm) {
    	for (int originRouteIndex = 0; originRouteIndex < routes.size(); originRouteIndex++) {
            Route rt1 = routes.get(originRouteIndex);
            for (int targetRouteIndex = 0; targetRouteIndex < routes.size(); targetRouteIndex++) {
                Route rt2 = routes.get(targetRouteIndex);

                for (int originNodeIndex = 1; originNodeIndex < rt1.getRouteNodes().size() - 1; originNodeIndex++) {
                    for (int targetNodeIndex = 0; targetNodeIndex < rt2.getRouteNodes().size() - 1; targetNodeIndex++) {
                        //Why? No change for the route involved
                        if (originRouteIndex == targetRouteIndex && (targetNodeIndex == originNodeIndex || targetNodeIndex == originNodeIndex - 1)) {
                            continue;
                        }

                        Node a = rt1.getRouteNodes().get(originNodeIndex - 1);
                        Node b = rt1.getRouteNodes().get(originNodeIndex);
                        Node c = rt1.getRouteNodes().get(originNodeIndex + 1);

                        Node insPoint1 = rt2.getRouteNodes().get(targetNodeIndex);
                        Node insPoint2 = rt2.getRouteNodes().get(targetNodeIndex + 1);

                        //capacity constraints
                        if (originRouteIndex != targetRouteIndex) {
                            if (b.getDemand() > rt2.getTruck().getRemainingCap()) {
                                continue;
                            }
                        }
                        double newRouteInHours = 0;

                        double costAdded = timeM[a.getNodeID()][c.getNodeID()] + timeM[insPoint1.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][insPoint2.getNodeID()];
                        double costRemoved = timeM[a.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][c.getNodeID()] + timeM[insPoint1.getNodeID()][insPoint2.getNodeID()];
                        double moveCost = costAdded - costRemoved;

                        double costChangeOriginRoute = timeM[a.getNodeID()][c.getNodeID()] - (timeM[a.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][c.getNodeID()]);
                        double costChangeTargetRoute = timeM[insPoint1.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][insPoint2.getNodeID()] - timeM[insPoint1.getNodeID()][insPoint2.getNodeID()];
                        double totalObjectiveChange = costChangeOriginRoute + costChangeTargetRoute;

                        newRouteInHours = costChangeOriginRoute + rt1.getTotalRouteTimeInHrs();
                        if (newRouteInHours <  (costChangeTargetRoute + rt2.getTotalRouteTimeInHrs())) {
                        	newRouteInHours = costChangeTargetRoute + rt2.getTotalRouteTimeInHrs();
                        }
                       
                        //Testing
                        if (Math.abs(moveCost - totalObjectiveChange) > 0.0001) {
                            int mn = 0;
                        }
                        newRouteInHours = CalculateMaxHoursInMove(newRouteInHours, originRouteIndex, targetRouteIndex);
                        StoreBestRelocationMove(originRouteIndex, targetRouteIndex, originNodeIndex, targetNodeIndex, rm, moveCost, newRouteInHours);
                    }
                }
            }
    	}
	}
	
    private void StoreBestRelocationMove(int originRouteIndex, int targetRouteIndex, int originNodeIndex, int targetNodeIndex, RelocationMove rm, double moveCost, double newRouteInHours) {

        if (newRouteInHours < rm.getMaxRouteTime()) {
            rm.setOriginNodePosition(originNodeIndex);
            rm.setOriginRouteIndex(originRouteIndex);
            rm.setTargetNodePosition(targetNodeIndex);
            rm.setTargetRoutePosition(targetRouteIndex);
            rm.setMoveCost(moveCost);
            rm.setMaxRouteTime(newRouteInHours);
        }
    }

	private void calculateMaxRoute()  {
		maxRoute = Collections.max(routes, Comparator.comparing(r -> r.getTotalRouteTimeInHrs()));
	}

    
    private void printFinalData() {
        for(Route r : routes) {
            System.out.println("Route " + r.getRouteID() + ": ");
            System.out.println(r.getRouteNodes().toString());
            System.out.print("Route duration in hours: ");
            System.out.printf("%.2f", r.getTotalRouteTimeInHrs());
            System.out.println("\n");
        }

        calculateMaxRoute();
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


    private void printMatrix(double[][] matrix) {
        System.out.print("\t");
        int i = 0;
        do {
            System.out.print(" " + i + "\t");
            i++;
        } while (i < allNodes.size());
        System.out.println();
        System.out.print("\t");
        for(i = 0; i < allNodes.size(); i++) {
            if (i != allNodes.size() - 1) {
                System.out.print("________");
            } else {
                System.out.print("____");
            }
        }
        System.out.println();
        for (i = 0; i < allNodes.size(); i++) {
            System.out.print(i + "\t" + "|");
            for (int j = 0; j < allNodes.size(); j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private void applyAdvanced() {
        for (int i = 0; i < allNodes.size()-1; i++) {
            ArrayList<Solution> potentialNodes = new ArrayList<Solution>();//keeps nodes that might change
            ArrayList<Route> nQuickestRoutes;
            do {
                nQuickestRoutes = findNQuickestRoutes(25);//if n = 25 solution= 7,47 time = 1 sec
                potentialNodes = new ArrayList<>(nQuickestRoutes.size());
                for (Route r : nQuickestRoutes) {
                    potentialNodes.add(findMinUnservicedPoint(r));
                }
            } while(potentialNodes.contains(null));
            Pair<Route, Node> winningPair = findBestAddition(nQuickestRoutes, potentialNodes);
            Route minScoreRoute = winningPair.getRoute();
            Node nodeToInsert = winningPair.getNode();
            minScoreRoute.addNodeToRoute(nodeToInsert, winningPair.getPositionInRoute());
            nodeToInsert.updateServiceStatus(true);
            int truckCap = minScoreRoute.getTruck().getRemainingCap();
            minScoreRoute.getTruck().setRemainingCap(truckCap - nodeToInsert.getDemand());
            minScoreRoute.setTotalRouteTimeInHrs( 
            			 winningPair.getCost() + 0.25D);
            }
    }

    private Pair<Route, Node> findBestAddition(ArrayList<Route> nQuickestRoutes, ArrayList<Solution> potentialSolution) {
    	Route minScoreRoute = null;
        Node nodeToInsert  = null;
        Solution s = null;
        double min = Double.MAX_VALUE;
        for(int j = 0; j < nQuickestRoutes.size(); j++) {
            Route r = nQuickestRoutes.get(j);
            double score = potentialSolution.get(j).getCost();
            if (nQuickestRoutes.size() > 2) {
            	score = r.getTotalRouteTimeInHrs() + potentialSolution.get(j).getCost();
            }
            if (score < min) {
                min = score;
                s =  potentialSolution.get(j);
                minScoreRoute = r;
                nodeToInsert  = s.getNode();
            }
        }
        return new Pair<Route, Node>(minScoreRoute, nodeToInsert, s.getPos(), s.getCost());
    }
    
    private  Solution findMinUnservicedPoint(Route currentQuickestRoute) {
    	Truck truck = currentQuickestRoute.getTruck();
        double min = Double.MAX_VALUE;
        int solutionInsertionPoint = -1;
        int minIndex = -1;
        if (currentQuickestRoute.getRouteNodes().size() == 1) {
        	for(int j = 1; j < allNodes.size(); j++) {
        		Node candidate = allNodes.get(j);
        		if (!candidate.isServiced() && candidate.getDemand() <= truck.getRemainingCap()) {
        				Node a = currentQuickestRoute.getRouteNodes().get(0);
        				double newCost = timeMatrix[a.getNodeID()][j];
        				if(newCost != 0D && newCost < min) {
        					min = newCost;
        					minIndex = j;
        					solutionInsertionPoint = 0;
        				}
        			}
        		}
        } else {
        	for(int j = 1; j < allNodes.size(); j++) {
        		Node candidate = allNodes.get(j);
        		if (candidate.isServiced() == false) {
        				for (int i = 0; i < currentQuickestRoute.getRouteNodes().size(); i++) {
        				    if (candidate.getDemand() <= truck.getRemainingCap()) {
        					    if (currentQuickestRoute.getRouteNodes().size() - 1 == i) {
        						    Node a = currentQuickestRoute.getRouteNodes().get(i);
        						    double newCost = timeMatrix[a.getNodeID()][j];
                				    if(newCost != 0D && newCost < min) {
                					    min = newCost;
                					    minIndex = j;
                					    solutionInsertionPoint = i;
                				    }
        					    } else {
        			
        							Node a = currentQuickestRoute.getRouteNodes().get(i);
        							Node b = currentQuickestRoute.getRouteNodes().get(i + 1);
        							double trialCost = timeMatrix[a.getNodeID()][j] + timeMatrix[j][b.getNodeID()]
        								- timeMatrix[a.getNodeID()][b.getNodeID()];
        							if(trialCost != 0D && trialCost < min) {
        								min = trialCost;
        								minIndex = j;
        								solutionInsertionPoint = i;
        							}
                                }
        					}
        				}
        			}
        		}
        	}
        
	

  
        if (minIndex != -1) {
        	Solution s = new Solution(allNodes.get(minIndex), solutionInsertionPoint, min);
            return s;
        } else {
            currentQuickestRoute.setFinalised(true);
            return null;
        }
    }
    
    private ArrayList<Route> findNQuickestRoutes(int n) {
        List<Route> availableRoutes = routes.stream().filter(t -> !t.isFinalised()).collect(Collectors.toList());
        List<Route> sortedList = availableRoutes.stream().sorted(Comparator.comparingDouble(Route::getTotalRouteTimeInHrs))
        		.collect(Collectors.toList());
        if (sortedList.size() >= n) {
            return new ArrayList<Route>(sortedList.subList(0, n));
        } else {
            return new ArrayList<Route>(sortedList);
        }
    }
    //method that creates timematrix based on distance.	
    private void convertDistanceToTime(double[][] distanceMatrix) {
        int numberOfRows = distanceMatrix[0].length;
        int numberOfCols = distanceMatrix[1].length;
        this.timeMatrix = new double[numberOfRows][numberOfCols];

        for(int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                timeMatrix[row][col] = distanceMatrix[row][col] / Truck.speedKMH;
            }
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new VehicleRoutingProblem(200,25);
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
