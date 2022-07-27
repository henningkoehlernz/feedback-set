package grasp;

import java.util.*;

/**
 * stores the data and performs problem-specific work
 */
public interface GRASPProblem extends Cloneable {

    /**
     * @return the objects available for greedy-selection
     */
    public Set getGreedyObjects();

    /**
     * informs the GRASP-problem of a selection
     * @param obj is one of the elements from getGreedyObjects
     */
    public void selectGreedyObject(GreedyObject obj);

    /**
     * performs local optimization
     */
    public void optimizeLocal();

    /**
     * @return the weight of a given solution
     */
    public double getSolutionWeight(Object solution);

    /**
     * will only be called when getGreedyObjects() is empty
     * @return a weight for the solution found; lower = better
     */
    public double getSolutionWeight();

    /**
     * @return the solution found
     */
    public Object getSolution();

    public Object clone();

}
