package fcr;

import java.util.*;

/**
 * A FCRAlgorithm consists of a FCRObject and a set of FCRSteps that can be performed on the FCRObject and are FCR
 * Finite Church-Rosser (FCR): The order in which the FCRSteps are applied does not affect the final result, and every sequence of FCRSteps terminates
 */
public interface FCRAlgorithm extends Observer {

    /**
     * registers a FCRStep as part of the algorithm
     * @param step the FCRStep to be registered
     */
    public void register(FCRStep step);

    /**
     * @return the FCRObject which the algorithm is currently being applied to
     */
    public FCRObject getObject();

    /**
     * runs the algorithm on the current FCRObject by applying registered FCRSteps until no more steps can be applied
     */
    public void run(FCRObject fcrObj);

}
