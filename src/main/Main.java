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
		String file = "instances/solomon_25/C101.txt";
		
		SolomonReader reader = new SolomonReader(instance, file);		
		reader.read();
		instance.build();
		
		// We reduce the number of vehicles
		instance.setVehicles(5);
		
		Solver solver = new Solver(instance);	
		solver.solveVRPTW();
	}

}
