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
		String directory = "instances/solomon_100/";
		String file = "R101.txt";
		int nbClients = 15;
		int nbVehicles = 5;

		
		SolomonReader reader = new SolomonReader(instance, directory+file);		
		reader.read(nbClients);
		instance.setVehicles(nbVehicles);
		instance.build();
		
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
