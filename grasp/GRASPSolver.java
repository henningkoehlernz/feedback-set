package grasp;

import java.util.*;

/**
 * performs the problem-independent GRASP work
 */
public class GRASPSolver {

    private RCLConstructor rclConstructor;
    protected Random rand = new Random();

    public GRASPSolver() {
	this(new WeightPercentageRCLConstructor(0.8));
    }

    public GRASPSolver(RCLConstructor rclConstructor) {
	this.rclConstructor = rclConstructor;
    }

    protected RCLConstructor getRCLConstructor() {
	return rclConstructor;
    }

    /**
     * @param iterations number of random solutions to check
     * @return the best found solution
     */
    public Object solve(GRASPProblem gp, int iterations) {

	Object bestSolution = null;
	double bestSolutionWeight = 0;

	for (int i = 0; i < iterations; i++) {
	    GRASPProblem clone = (GRASPProblem)gp.clone();
	    double newWeight = iterateGRASP(clone);
	    // Log.debug("weight=" + newWeight);
	    // better result than old one?
	    if (bestSolution == null || newWeight < bestSolutionWeight) {
		bestSolution = clone.getSolution();
		bestSolutionWeight = newWeight;
	    }
	}

	return bestSolution;
    }

    /**
     * performs a single GRASP iteration; result will be stored in gp
     * @return weight of the solution found as given by gp.getSolutionWeight()
     */
    protected double iterateGRASP(GRASPProblem gp) {
	while (true) {
	    Set rclSet = getRCLConstructor().constructRCL(gp.getGreedyObjects());
	    // are we done?
	    if (rclSet.size() == 0)
		break;
	    // randomly select edge to remove from rcl
	    GreedyObject[] rcl = (GreedyObject[])rclSet.toArray(new GreedyObject[0]);
	    gp.selectGreedyObject(rcl[rand.nextInt(rcl.length)]);
	}
	// found a solution - now do local optimization
	gp.optimizeLocal();
	return gp.getSolutionWeight();
    }
}
