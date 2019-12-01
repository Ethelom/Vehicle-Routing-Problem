import java.util.ArrayList;
import java.util.Random;

public class Main {
	private static ArrayList<Truck> totalTrucks;
	private static ArrayList<Node> allNodes;
	private static ArrayList<Node> servicePoints;
	private static double[][] distance;
	private static double[][] times;
	private static Node depot;
	
	public static void main(String args[]) {
		Main m = new Main();
		ArrayList<Node> allNodes = m.createAllNodesAndServicePointLists();
		m.distanceOfNodes(allNodes);
		m.calculateTimes();
		m.createTrucks();
		m.applyVRP();
		System.out.println(m.findLongestPath());
	}
	
	private double findLongestPath() {
		double longestTime = Double.MIN_VALUE;
		Truck truck = null;
		for (int i =0 ; i < 25; i++) {
			if (totalTrucks.get(i).getTotalTime() > longestTime) {
				longestTime = totalTrucks.get(i).getTotalTime();
				truck = totalTrucks.get(i);
			}
		}
		return longestTime;
	}
	
	private void applyVRP() {
		
        for (int i = 0; i < servicePoints.size(); i++) {
            	double bestTimeForTruck = Double.MAX_VALUE;
                int solutionInsertionPoint = -1;
                int positionInTrucks = -1; 
            	for (int p = 0 ; p < 25; p++) {
            		Node candidate = servicePoints.get(i);
            		ArrayList<Node> nodeSequence  = totalTrucks.get(p).getRoute();
            		if (candidate.isRouted() == false) {
            			for (int k = 0; k < nodeSequence.size() -1; k++)
            			{
            				Node A = nodeSequence.get(k);
            				Node B = nodeSequence.get(k + 1);
            				double trialTime = times[A.getID()][candidate.getID()] 
                        		+ times[candidate.getID()][B.getID()] - times[A.getID()][B.getID()];
            				if (trialTime < bestTimeForTruck && totalTrucks.get(p).addToTruck(servicePoints.get(i).getDemand())) {
            					bestTimeForTruck = trialTime;
            	                solutionInsertionPoint = k;
            	                positionInTrucks = p;
            				}
                        }
            		}
            	}
            	Node insertedNode = servicePoints.get(i);
            	totalTrucks.get(positionInTrucks).addToRoute(insertedNode, bestTimeForTruck, solutionInsertionPoint);
            }
    }
	private void createTrucks() {
		totalTrucks = new ArrayList<Truck>();
		Truck t = new Truck(1);
		t.addToRoute(depot,0);
		t.addToRoute(depot,0);
		totalTrucks.add(t);
		for (int i = 1; i < 25; i++) {
			t = new Truck(i + 1);
			t.addToRoute(depot,0);
			t.addToRoute(depot,0);
			totalTrucks.add(t);
		}
	}
	private void calculateTimes() {
		times = new double[allNodes.size()][allNodes.size()];
		for (int i = 0; i< distance.length; i++) {
			for (int j = 0; j< distance.length; j++) {
				times[i][j] = distance[i][j] / 35;
			}
		}
		
	}

	public void distanceOfNodes(ArrayList<Node> allNodes) {
		distance = new double[allNodes.size()][allNodes.size()];
		 for (int i = 0 ; i < allNodes.size(); i++)
	        {
	            Node from = allNodes.get(i);
	            
	            for (int j = 0 ; j < allNodes.size(); j++)
	            {
	                Node to = allNodes.get(j);
	                
	                double Delta_x = (from.getX() - to.getX());
	                double Delta_y = (from.getY() - to.getY());
	                double dis = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));
	                dis = Math.round(dis);
	                distance[i][j] = dis;
	            }
	        }	
		 }
	public ArrayList<Node> createAllNodesAndServicePointLists() {     
		//Create the list with the service points         
		servicePoints = new ArrayList<Node>();         
		Random ran = new Random(1);         
		for (int i = 0 ; i < 200; i++) {
			Node sp = new Node();
			sp.setX(ran.nextInt(100));
			sp.setY(ran.nextInt(100));
			sp.setDemand(100*(1 + ran.nextInt(5)));
			sp.setServiceTime(0.25);
			servicePoints.add(sp);
		}         
		//Build the allNodes array and the corresponding distance matrix 
		allNodes = new ArrayList<Node>();
		depot = new Node();
		depot.setX(50);
		depot.setY(50);
		depot.setDemand(0);
		allNodes.add(depot);
		for (int i = 0 ; i < servicePoints.size(); i++) {
			Node cust = (Node) servicePoints.get(i);
			allNodes.add(cust);
		}                 
		for (int i = 0 ; i < allNodes.size(); i++) {
			Node nd = (Node) allNodes.get(i);
			nd.setID(i);
		}
		
		return allNodes;
	} 
}