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
    private double[][] distanceMatrix, timeMatrix, timeM;
    private Route maxRoute;

    public VehicleRoutingProblem(int totalServicePoints, int totalTrucks) {
        this.totalServicePoints = totalServicePoints;
        this.totalTrucks = totalTrucks;
        generateRandomNetwork();
        createTrucks();
        initializeRoutes();
        convertDistanceToTime(distanceMatrix);
        applyAdvanced();
        addDepositoryInEnd();
        printFinalData();
        boolean endMove = false;
        CalculateTimeWithoutReturnToDepo();
        while (!endMove) {
        	RelocationMove rm = new RelocationMove();
        	applyBestRelocationMove(rm);
        	if (rm.getMoveCost() > -0.00001) {
        		endMove = true;
        	} else {
        		ApplyMove(rm);
        		calculateMaxRoute();
        		System.out.println(" Relocation: " + maxRoute.getTotalRouteTimeInHrs()+ "route:" + maxRoute.getRouteNodes().toString());
            }
        }
    }
    
    private void CalculateTimeWithoutReturnToDepo() {
    	timeM = new double[totalServicePoints+1][totalServicePoints+1];
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

	private void ApplyMove(RelocationMove rm) {
		if (rm.getMoveCost() == Double.MAX_VALUE) {
            return;
        }
		Route targetRoute = routes.get(rm.getTargetRoutePosition());
		Node B = maxRoute.getRouteNodes().get(rm.getOriginNodePosition());
		if (maxRoute == targetRoute) {
			maxRoute.getRouteNodes().remove(rm.getOriginNodePosition());
            if (rm.getOriginNodePosition() < rm.getTargetNodePosition()) {
                targetRoute.getRouteNodes().add(rm.getTargetNodePosition(), B);
            } else {
                targetRoute.getRouteNodes().add(rm.getTargetNodePosition() + 1, B);
            }
            System.out.println("maxroute: "+maxRoute.getTotalRouteTimeInHrs());
            maxRoute.setTotalRouteTimeInHrs(rm.getMoveCost());
		} else {
            Node A = maxRoute.getRouteNodes().get(rm.getOriginNodePosition() - 1);
            Node C = maxRoute.getRouteNodes().get(rm.getOriginNodePosition() + 1);

            Node F = targetRoute.getRouteNodes().get(rm.getOriginNodePosition());
            Node G = targetRoute.getRouteNodes().get(rm.getOriginNodePosition() + 1);

            double costChangeOrigin = timeM[A.getNodeID()][C.getNodeID()] - timeM[A.getNodeID()][B.getNodeID()] - timeM[B.getNodeID()][C.getNodeID()];
            double costChangeTarget = timeM[F.getNodeID()][B.getNodeID()] + timeM[B.getNodeID()][G.getNodeID()] - timeM[F.getNodeID()][G.getNodeID()];
            System.out.println(maxRoute.getTruck().getRemainingCap() - B.getDemand()+ ","+targetRoute.getTruck().getRemainingCap() + B.getDemand());
            maxRoute.getTruck().setRemainingCap(maxRoute.getTruck().getRemainingCap() - B.getDemand());
            targetRoute.getTruck().setRemainingCap(targetRoute.getTruck().getRemainingCap() + B.getDemand()); 

            maxRoute.setTotalRouteTimeInHrs(costChangeOrigin);
            targetRoute.setTotalRouteTimeInHrs(costChangeTarget);
            maxRoute.getRouteNodes().remove(rm.getOriginNodePosition());
            targetRoute.getRouteNodes().add(rm.getTargetNodePosition() + 1, B);
		}
	}

    
    private void addDepositoryInEnd() {
		for (int i = 0 ; i < routes.size(); i++) {
			routes.get(i).getRouteNodes().add(depository);
		}
		
	}
    
    private void applyBestRelocationMove(RelocationMove rm) {
    	int originRouteIndex = routes.indexOf(maxRoute);
		for (int targetRouteIndex = 0; targetRouteIndex < routes.size(); targetRouteIndex++) {
			Route rt2 = routes.get(targetRouteIndex);
			for (int originNodeIndex = 1; originNodeIndex < maxRoute.getRouteNodes().size() - 1; originNodeIndex++) {
                for (int targetNodeIndex = 0; targetNodeIndex < rt2.getRouteNodes().size() - 1; targetNodeIndex++) {
                	if (originRouteIndex == targetRouteIndex && (targetNodeIndex == originNodeIndex || targetNodeIndex == originNodeIndex - 1)) {
                		System.out.println("to continue: " + targetRouteIndex);
                		continue;
                    }
                	Node a = maxRoute.getRouteNodes().get(originNodeIndex - 1);
                    Node b = maxRoute.getRouteNodes().get(originNodeIndex);
                    Node c = maxRoute.getRouteNodes().get(originNodeIndex + 1);

                    Node insPoint1 = rt2.getRouteNodes().get(targetNodeIndex);
                    Node insPoint2 = rt2.getRouteNodes().get(targetNodeIndex + 1);
                    if (originRouteIndex != targetRouteIndex) {
                    	System.out.println("inside if for different routes");
                        if (rt2.getTruck().getRemainingCap() - b.getDemand() >= 0) {
                        	System.out.println("inside if not enough cap");
                            continue;
                        }
                    }
                    double costAdded = timeM[a.getNodeID()][c.getNodeID()] + timeM[insPoint1.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][insPoint2.getNodeID()];
                    double costRemoved = timeM[a.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][c.getNodeID()] + timeM[insPoint1.getNodeID()][insPoint2.getNodeID()];
                    double moveCost = costAdded - costRemoved;
                    double newRouteInHours = 0;
                    
                    double costChangeOriginRoute = timeM[a.getNodeID()][c.getNodeID()] - (timeM[a.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][c.getNodeID()]);
                    double costChangeTargetRoute = timeM[insPoint1.getNodeID()][b.getNodeID()] + timeM[b.getNodeID()][insPoint2.getNodeID()] - timeM[insPoint1.getNodeID()][insPoint2.getNodeID()];

                    double newOldMaxRouteInHours = 0;
                    if (originRouteIndex == targetRouteIndex){
                    	System.out.println("inside equal route");
                    	newOldMaxRouteInHours = moveCost + maxRoute.getTotalRouteTimeInHrs();
                    } else {
                    	System.out.println("inside NOT equal route");
                    	newRouteInHours = costChangeTargetRoute + rt2.getTotalRouteTimeInHrs();
                    	newOldMaxRouteInHours = costChangeOriginRoute + maxRoute.getTotalRouteTimeInHrs();
                    }
                    double maxHoursOfRoute = CalculateMaxHoursInMove(newRouteInHours, newOldMaxRouteInHours, originRouteIndex, targetRouteIndex);
                    System.out.println("Max Route Hours of this relocation:" + maxHoursOfRoute);
                    if (maxHoursOfRoute < rm.getMaxRouteTime())
                        rm.setOriginNodePosition(originNodeIndex);
                        rm.setTargetNodePosition(targetNodeIndex);
                        rm.setTargetRoutePosition(targetRouteIndex);
                        rm.setMaxRouteTime(maxHoursOfRoute);
                        rm.setMoveCost(moveCost);
                    }
                }
			}
		System.out.println(rm.getMaxRouteTime());
		}
	
    
    private double CalculateMaxHoursInMove(double newRouteInHours, double newOldMaxRouteInHours, int originRouteIndex,
			int targetRouteIndex) {
		double maxHours = newRouteInHours;
		boolean b = (originRouteIndex == targetRouteIndex);
		if (newOldMaxRouteInHours > maxHours) {
			maxHours = newOldMaxRouteInHours;
		}
    	for (int i = 0; i < routes.size(); i++) {
    		if (b) {
    			if (i != originRouteIndex) {
    				if (routes.get(i).getTotalRouteTimeInHrs() > maxHours) {
    					maxHours = routes.get(i).getTotalRouteTimeInHrs();
    				}
    			}
    		} else {
    			if ((i != originRouteIndex) && (i != targetRouteIndex)) {
    				if (routes.get(i).getTotalRouteTimeInHrs() > maxHours) {
    					maxHours = routes.get(i).getTotalRouteTimeInHrs();
    				}
    			}
    			
    		}
    	}
		return maxHours;
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
            ArrayList<Solution> potentialNodes = new ArrayList<Solution>();
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
