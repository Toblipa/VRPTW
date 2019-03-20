package model;

public class VrptwInstance {

	private Customer[] customers;
	
	private int[][] adjacency;
	
	private double[][] cost;
	
	private int vehicles;
	
	private double capacity;
	
	/**
	 * Calculate the costs considering Time Service and Distance
	 */
	public void build() {
        int nbCustomers = this.customers.length;
        
        // We build the cost of each arc
        this.cost = new double[nbCustomers][nbCustomers];
        for(int i=0; i < nbCustomers; i++) {
            for(int j=0; j < nbCustomers; j++) {
                this.cost[i][j] = this.customers[i].distance(this.customers[j]);
            }
        }
        
        // We build the adjacency matrix
        this.adjacency = new int[nbCustomers][nbCustomers];
		for(int i = 0; i < this.customers.length; i++) {
			Customer node = this.customers[i];
		
			// We check every node to see if it is a valid successor
			for(int j = 1; j < this.customers.length; j++) {
				Customer nextCustomer = this.customers[j];
				
				// We compute the time needed to reach the node which corresponds to
				// the minimal time to complete the service in the current node + the time needed to get to the next node
				double timeToReach = node.getStart() + node.getServiceTime() + this.cost[node.getCustomerId()][nextCustomer.getCustomerId()];
				
				// Check if it is possible
				if( nextCustomer.getEnd() >= timeToReach ) {
					this.adjacency[i][j] = 1;
				}
				else {
					this.adjacency[i][j] = 0;
				}
			}
		}
	}
	
	public Customer[] getCustomers() {
		return customers;
	}

	public void setCustomers(Customer[] customers) {
		this.customers = customers;
	}

	public double[][] getCost() {
		return cost;
	}

	public void setCost(double[][] cost) {
		this.cost = cost;
	}

	public int getVehicles() {
		return vehicles;
	}

	public void setVehicles(int vehicles) {
		this.vehicles = vehicles;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public int[][] getAdjacency() {
		return adjacency;
	}

	public void setAdjacency(int[][] adjacency) {
		this.adjacency = adjacency;
	}
}
