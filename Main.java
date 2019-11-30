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
		ArrayList<Node> allNodes = m.CreateAllNodesAndServicePointLists();
		m.DistanceOfNodes(allNodes);
		m.totalTrucks = new ArrayList<Truck>();
		m.CalculateTimes();
		
	}
	
	private void CalculateTimes() {
		times = new double[allNodes.size()][allNodes.size()];
		for (int i = 0; i< distance.length; i++) {
			for (int j = 0; j< distance.length; j++) {
				times[i][j] = distance[i][j] / 35;
			}
		}
		
	}

	public void DistanceOfNodes(ArrayList<Node> allNodes) {
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
	public ArrayList<Node> CreateAllNodesAndServicePointLists() {     
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
