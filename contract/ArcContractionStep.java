package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;

/**
 * A ContractionStep object describes a single contraction step type applicable to a ContractionGraph
 */
public abstract class ArcContractionStep extends ContractionStep {

    /**
     * initializes the FCRStep as part of an FCRAlgorithm
     * computes the initially applicable transformations
     */
    public void initApply() {
	ContractGraph g =  (ContractGraph)alg.getObject();
	applications.clear();
	//applications.addAll(g.edgeSet());
 	Iterator it = g.edgeSet().iterator();
	while (it.hasNext())
	    store(it.next());
   }

    protected void apply(Object obj, ContractGraph g) {
	apply((Edge)obj, g);
    };

    protected boolean canApply(Object obj, ContractGraph g) {
	return canApply((Edge)obj, g);
    };

    protected abstract void apply(Edge edge, ContractGraph g);
   
    protected abstract boolean canApply(Edge edge, ContractGraph g);

}
