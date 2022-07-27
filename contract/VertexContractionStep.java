package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;

/**
 * A ContractionStep object describes a single contraction step type applicable to a ContractionGraph
 */
public abstract class VertexContractionStep extends ContractionStep {

    /**
     * initializes the FCRStep as part of an FCRAlgorithm
     * computes the initially applicable transformations
     */
    public void initApply() {
	ContractGraph g =  (ContractGraph)alg.getObject();
	applications.clear();
	//applications.addAll(g.vertexSet());
	Iterator it = g.vertexSet().iterator();
	while (it.hasNext())
	    store(it.next());
    }
}
