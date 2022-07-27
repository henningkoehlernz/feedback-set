package grasp;

import java.util.*;

/**
 * constructs the rcl for greedy selection
 */
public interface RCLConstructor {

    /**
     * @param greedyObjects must contains elements of type GreedyObject
     * @return a subset of greedyObjects with high greedyWeight
     */
    public Set constructRCL(Set greedyObjects);

}
