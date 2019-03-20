package solver;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.Customer;
import model.VrptwInstance;

public class Solver {
	VrptwInstance instance;
	
	public Solver(VrptwInstance instance) {
		this.instance = instance;
	}
	
	public void solveVRPTW() {
        try {
            IloCplex cplex = new IloCplex();
            cplex.setParam(IloCplex.DoubleParam.TiLim, 300);

            // Decision variables
            IloNumVar[][][] x = new IloNumVar[instance.getVehicles()][instance.getCustomers().length][instance.getCustomers().length];
            for(int u=0; u < x.length; u++) {
	            for(int i=0; i < x[u].length; i++) {
	                for(int j=0; j < x[u][i].length; j++) {
	                    x[u][i][j] = cplex.boolVar("x^"+u+"_"+i+"_"+j);
	                }
	            }
            }
            
            // Objective
            this.addObjective(cplex, x);
            
            // Constraints
            this.addVisitedNodesConstraints(cplex, x);
            
            this.addFluxConstraints(cplex, x);
            
            this.addOneTourConstraints(cplex, x);
            
            this.addCapacityConstraints(cplex, x);
            
            this.addTimeWindowsConstraints(cplex, x);
            
            // Export the model into a file
            cplex.exportModel("VrptwModel.lp");

            // Solve
            cplex.solve();

            // Display results
            this.displayRoutesResult(cplex, x);

            
        } catch (IloException ex) {	
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	/**
	 * Showing results in form of a matrix
	 * 
	 * @param cplex
	 * @param x
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	@SuppressWarnings("unused")
	private void displayMatrixResult(IloCplex cplex, IloNumVar[][][] x) throws UnknownObjectException, IloException {
        System.out.println("Solution:");
        
        for (int u = 0; u < x.length; u++) {
        	System.out.println("Printing matrix route for vehicle " + u+1);
        	for (int i = 0; i < x[u].length; i++) {
        		for(int j = 0; j < x[u][i].length; j++) {
        			System.out.print( Math.abs((int) cplex.getValue( x[u][i][j]) ) + " ");
        		}
        		System.out.println("");
        	}
        }
	}
	
	/**
	 * Showing results in form of a route
	 * 
	 * @param cplex
	 * @param x
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	@SuppressWarnings("unused")
	private void displayRoutesResult(IloCplex cplex, IloNumVar[][][] x) throws UnknownObjectException, IloException {
        System.out.println("Solution:");
        
        for (int u = 0; u < x.length; u++) {
        	// Every vehicle starts from the origin node
        	int currentNode = 0;
        	
    		System.out.print("Vehicle "+(u+1));
        	// While the vehicle has not returned to the depot
    		boolean finished = false;
        	while ( !finished ) {
        		for(int i = 0; i < x[u][currentNode].length; i++) {
        			if( cplex.getValue(x[u][currentNode][i]) > 0 ) {
        				if(currentNode == 0) {
        					System.out.println(" did the following route:");
        				}
        				System.out.print(currentNode + " => ");
        				
        				currentNode = i;
        				
        				if(i == 0) {
        					finished = true;
        		        	System.out.println("Depot");
        				}
        				
        				break;
        			}
        			
        			if(i == x[u][currentNode].length - 1) {
        				finished = true;
        				System.out.println(" has not started any route.");
        			}
        		}	
        	}
        	System.out.println("");
        }
        
        System.out.println("Total cost:" + cplex.getObjValue());
	}


	/**
	 * Time Windows constraints 
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addTimeWindowsConstraints(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		// A large number
		double M = 10000;
		
		// The time s^u_i where customer i is served by vehicle u
        IloNumVar[][] s = new IloNumVar[instance.getVehicles()][instance.getCustomers().length];
        for(int u=0; u < s.length; u++) {
            for(int i=0; i < s[u].length; i++) {
            	Customer currentNode = this.instance.getCustomers()[i];
            	s[u][i] = cplex.numVar(currentNode.getStart(), currentNode.getEnd(), "s^"+u+"_"+i);
            }
        }
        
        // Time windows when visiting a node constraints
        for(int u=0; u < s.length; u++) {
            for(int i=0; i < s[u].length; i++) {
            	Customer currentNode = this.instance.getCustomers()[i];
            	
            	for(int j=1; j < s[u].length; j++) {
            		IloLinearNumExpr expression = cplex.linearNumExpr();
            		
            		expression.addTerm(1.0, s[u][i]);
                	expression.addTerm(-1.0, s[u][j]);
                	expression.addTerm(M, x[u][i][j]);
                	                	
                	cplex.addLe(expression, M - currentNode.getServiceTime() - this.instance.getCost()[i][j]);	
            	}
            	
            }
        }
        
        // Vehicles must return to the depot during the given time constraints
        for(int u=0; u < s.length; u++) {
            for(int i=0; i < s[u].length; i++) {
            	Customer currentNode = this.instance.getCustomers()[i];
            	
        		IloLinearNumExpr expression = cplex.linearNumExpr();
        		
        		expression.addTerm(1.0, s[u][i]);
            	expression.addTerm(M, x[u][i][0]);
            	
            	cplex.addLe(expression, M - currentNode.getServiceTime() - this.instance.getCost()[i][0] + this.instance.getCustomers()[0].getEnd());            	
            }
        }
        
        // Respect start and end service time constraints
        for(int u=0; u < s.length; u++) {
            for(int i=0; i < s[u].length; i++) {
            	Customer currentNode = this.instance.getCustomers()[i];
            	IloLinearNumExpr expression = cplex.linearNumExpr();
            	
            	expression.addTerm(1.0, s[u][i]);
            	
            	cplex.addRange(currentNode.getStart(), expression, currentNode.getEnd());
            }
        }
		
	}
	
	/**
	 * Vehicles must not exceed capacity
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addCapacityConstraints(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		for (int u = 0; u < x.length; u++) {
			IloLinearNumExpr expression = cplex.linearNumExpr();
			
			for(int i = 1; i < x[u].length; i++) {
				for(int j = 1; j < x[u][i].length; j++) {
					expression.addTerm(this.instance.getCustomers()[i].getDemand(), x[u][i][j]);
				}
			}
			
			cplex.addLe(expression, this.instance.getCapacity());
		}	
	}
	
	/**
	 * Vehicles can just go out once
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addOneTourConstraints(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		for (int u = 0; u < x.length; u++) {
			IloLinearNumExpr expression = cplex.linearNumExpr();
			
			for(int i = 1; i < x[u].length; i++) {
				expression.addTerm(1.0, x[u][0][i]);
			}
			
			cplex.addLe(expression, 1.0);
		}		
	}
	/**
	 * When arriving to a node we must leave
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addFluxConstraints(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		for (int u = 0; u < x.length; u++) {
			for (int i = 0; i < x[u].length; i++) {
				IloLinearNumExpr expression = cplex.linearNumExpr();
				
				for(int j = 0; j < x[u][i].length; j++) {
					if( i != j ) {
						expression.addTerm(1.0, x[u][i][j]);
						expression.addTerm(-1.0, x[u][j][i]);
					}
				}
				
				cplex.addEq(expression, 0.0);
			}	
		}		
	}
	
	/**
	 * Nodes must be visited at least once
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addVisitedNodesConstraints(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		for (int i = 1; i < this.instance.getCustomers().length; i++) {
			IloLinearNumExpr expression = cplex.linearNumExpr();
			
			for (int u = 0; u < x.length; u++) {
				for(int j = 0; j < x[u][i].length; j++)
				expression.addTerm(1.0, x[u][i][j]);
			}

			cplex.addGe(expression, 1.0);
		}
		
	}
	
	/**
	 * We minimize cost
	 * 
	 * @param cplex
	 * @param x
	 * @throws IloException
	 */
	private void addObjective(IloCplex cplex, IloNumVar[][][] x) throws IloException {
		IloLinearNumExpr obj = cplex.linearNumExpr();
		
		for(int u=0; u < x.length; u++) {
			for(int i=0; i < x[u].length; i++) {
				for(int j=0; j < x[u][i].length; j++) {
					if(i != j) {
						obj.addTerm(x[u][i][j], this.instance.getCost()[i][j]);
					}
				}
			}
		}
		
		cplex.addMinimize(obj);
		
	}
}
