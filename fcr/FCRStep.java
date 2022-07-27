package fcr;

import java.util.*;

/**
 * A FCRStep object describes a sub-relation within a FCR system.
 * It's purpose is to handle a (infinite) set of "similar" transformations that can be performed on FCRObjects.
 * A FCRStep object must keep track of applicable transformations
 */
public interface FCRStep extends Observer {

    /**
     * allows the step object to store the algorithm it belongs to - called when the step is being registered
     */
    public void setAlgorithm(FCRAlgorithm alg);

    /**
     * initializes the FCRStep as part of an FCRAlgorithm
     * computes the initially applicable transformations
     */
    public void initApply();

    /**
     * applies a single transformation to the FCRObject provided by the FCRAlgorithm if possible
     * @return TRUE iff a transformation could be applied
     */
    public boolean apply();

}
