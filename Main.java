
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Main {
	public ArrayList trucks = new ArrayList();
	public ArrayList allNodes = new ArrayList();
	double[][] distance;

	public static void main(String[] args) {
		Main a = new Main();
		ArrayList allNodes = new ArrayList();
		allNodes = a.CreateAllNodesAndServicePointLists();
		double[][] distance = a.distanceOfNodes(allNodes);
		ArrayList finalRoad = a.finalRoad(distance);
		//System.out.print(distance);
		
	}
	/*
	 * Method used to put the @demand of each node to an array.
	 */
	public int[] findDemand(ArrayList allNodes) {
		int[] demand = null;
		for (int i = 0; i < allNodes.size(); i++) {
			Node nd1 = (Node) allNodes.get(i);
			int d = nd1.demand;
			demand[i] = d;
		}
		Arrays.sort(demand);
		Collections.reverse(Arrays.asList(demand));
		return demand;
			
	}
	/*
	 * Method used to calculate the road each truck is going to get.
	 */
	public ArrayList finalRoad(double[][] distance) {
		int truckid = 1;
		int[] belongtotruck = null;
		Truck tr = new Truck(truckid);
		Node node = new Node();
		Main a = new Main();
		int[] demand = a.findDemand(allNodes);
		ArrayList finalroad = new ArrayList();
		Main main = new Main();
		while (truckid <= 25) {
		for (int i = 0; i < distance.length;i++) {
			if (tr.addToTruck(demand[i])) {//if truck has space
				if (distance.length > 0) {
					int position = main.findSmallerDistance(distance[i]);
					i = position;
					finalroad.add(position);
					belongtotruck[i] = truckid;
					distance = main.removeLine(distance,position);	
					demand = main.removeDemand(demand, i);
				}
			}
			if (!(tr.addToTruck(demand[i]))) {//else open new truck to fill
				truckid++;
				Truck tr1 = new Truck(truckid);
				if (distance.length > 0) {
					int position = main.findSmallerDistance(distance[i]);
					i = position;
					finalroad.add(position);
					belongtotruck[i] = truckid; 
					distance = main.removeLine(distance,position);	
					demand = main.removeDemand(demand, i);
				}
			}
		}
		}
		return finalroad;
	}
	public int[] removeDemand(int[] demand, int index) {
		int[] newdemand = null;
		int k = 0; 
		for (int i = 0; i < demand.length; i++) {  
            if (i == index) { 
            	continue; //We don't use the column with the position. 
            }
            newdemand[k] = demand[i];
            k++;
		}
            return newdemand;
	}
	
	/*
	 * Method to remove the added node from the @distance array.
	 */
	public double[][] removeLine(double[][] distance,int index){     
        // Create another array of size one less 
        double[][] anotherArray = new double[distance.length-1][distance.length - 1]; 
        
        // Copy the elements except the index 
        // from original array to the other array 
        int k1 = 0;
        int k2 = 0;
        for (int i = 0; i < distance.length; i++) {  
            if (i == index) { 
            	continue; //We don't use the column with the position. 
            } 
  
            // if the index is not 
            // the removal element index 
            for (int j = 0;j < distance[i].length;j++) {
            	if (j == index) {
            		continue;//We don't use the row with the position.
            	}
            	anotherArray[k1][k2] = distance[i][j];
            	k1++;
            	k2++;
            }
             
        } 
  
        // return the resultant array 
        return anotherArray; 
	}
	
	/*
	 * Method used to find the smaller distance in a certain line of an array.
	 * @param nodes is an array that includes the neighbours of given node.
	 */
	public int findSmallerDistance(double[] nodes) {
		double min = nodes[0];
		int position = 0;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] <= min) {
				min = nodes[i];
				position = i;
			}
		}
		return position;
	}
	
	public ArrayList CreateAllNodesAndServicePointLists() {     
		//Create the list with the service points         
		ArrayList servicePoints = new ArrayList();         
		Random ran = new Random(1);         
		for (int i = 0 ; i < 200; i++) {
			Node sp = new Node();
			sp.x = ran.nextInt(100);
			sp.y = ran.nextInt(100);
			sp.demand = 100*(1 + ran.nextInt(5));
			sp.serviceTime = 0.25;
			servicePoints.add(sp);
		}         
		//Build the allNodes array and the corresponding distance matrix 
		ArrayList allNodes = new ArrayList();
		Node depot = new Node();
		depot.x = 50;
		depot.y = 50;
		depot.demand = 0;
		allNodes.add(depot);
		for (int i = 0 ; i < servicePoints.size(); i++) {
			Node cust = (Node) servicePoints.get(i);
			allNodes.add(cust);
		}                 
		for (int i = 0 ; i < allNodes.size(); i++) {
			Node nd = (Node) allNodes.get(i);
			nd.ID = i;
		}
		return allNodes;
	} 
	/*
	 * Method used to calculate an 2*2 array with the distances of nodes.
	 * @param distance is an array with the distances.
	 */
	public double[][] distanceOfNodes(ArrayList allNodes) {
		this.distance = new double [allNodes.size()][allNodes.size()];
		for (int i = 0; i < allNodes.size(); i++) {
			Node nd1 = (Node) allNodes.get(i);
			int x1 = nd1.x;
			int y1 = nd1.y;
			for (int j = 0; j < allNodes.size();j++) {
				Node nd2 = (Node) allNodes.get(i);
				int x2 = nd2.x;
				int y2 = nd2.y;
				double d = Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((y2-y1), 2));
				distance[i][j] = d;
			}
		}
		return distance;
	}
}

