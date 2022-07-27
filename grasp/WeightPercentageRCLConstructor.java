package grasp;

import java.util.*;

/**
 * constructs the rcl for greedy selection
 */
public class WeightPercentageRCLConstructor implements RCLConstructor {
    
    private double percentage;

    public WeightPercentageRCLConstructor(double percentage) {
	this.percentage = percentage;
    }

    /**
     * @param greedyObjects must contains elements of type GreedyObject
     * @return the subset of all greedyObjects with weight >= maxWeight * percentage
     */
    public Set constructRCL(Set greedyObjects) {
	// find max weight
	double maxWeight = 0;
	Iterator it = greedyObjects.iterator();
	while (it.hasNext())
	    maxWeight = Math.max(maxWeight, ((GreedyObject)it.next()).getGreedyWeight());
	// collect objects to return
	Set result = new HashSet();
	double threshold = maxWeight * percentage;
	it = greedyObjects.iterator();
	while (it.hasNext()) {
	    GreedyObject obj = (GreedyObject)it.next();
	    if (obj.getGreedyWeight() >= threshold)
		result.add(obj);
	}
	return result;
    }

}
