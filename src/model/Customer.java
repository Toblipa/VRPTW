package model;

public class Customer {
	
	private double x;

	private double y;

	private int customerId;

	private int demand;

	private double start;

	private double end;

	private double serviceTime;
	
	public Customer() {
		this.x = 0;
		this.y = 0;
	}
	
	public Customer(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "Point{" + "x=" + x + ", y=" + y + '}';
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public int getDemand() {
		return demand;
	}

	public void setDemand(int demand) {
		this.demand = demand;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public double getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}
	
	/**
	 * Euclidian distance
	 * @param p
	 * @return
	 */
	public double distance (Customer p) {
		if(p==null) {
			return Double.MAX_VALUE;
		}
		double dx = this.getX() - p.getX();
		double dy = this.getY() - p.getY();
		return Math.sqrt(dx*dx + dy*dy);
	}
}
