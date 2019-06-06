package main;

import model.VrptwInstance;
import reader.SolomonReader;
import solver.Solver;

public class Main {

	public static void main(String[] args) {
		solomonVRPTW();
	}
	
	public static void solomonVRPTW() {
		VrptwInstance instance = new VrptwInstance();
		String file = "instances/solomon_25/R104.txt";
		
		SolomonReader reader = new SolomonReader(instance, file);		
		reader.read();
		instance.build();
		
//		printCostMatrix(instance.getCost());
		
		// We reduce the number of vehicles
		instance.setVehicles(10);
		
		Solver solver = new Solver(instance);	
		solver.solveVRPTW();
	}
	
	@SuppressWarnings("unused")
	private static void printCostMatrix(double[][] cost) {
		for( int i = 0; i < cost.length; i++ ) {
			for( int j= 0; j < cost[i].length; j++ ) {
				System.out.print( Math.floor(cost[i][j]*10)/10+" ");
			}
			System.out.println("");
		}
		
	}
}
