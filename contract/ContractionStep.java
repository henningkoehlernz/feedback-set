package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;

/**
 * A ContractionStep object describes a single contraction step type applicable to a ContractGraph
 */
public abstract class ContractionStep implements FCRStep {

    protected FCRAlgorithm alg;
    protected LinkedList applications = new LinkedList();
    
    /**
     * allows the step object to store the algorithm it belongs to - called when the step is being registered
     */
    public void setAlgorithm(FCRAlgorithm alg) {
	this.alg = alg;
    }

    /**
     * applies a single contraction to the ContractGraph provided by the FCRAlgorithm if possible
     * @return TRUE iff a contraction could be applied
     */
    public boolean apply() {
	ContractGraph g = (ContractGraph)alg.getObject();
	//System.out.println("apply-list(" + this + "): " + applications);
	while (!applications.isEmpty()) {
	    Object obj = applications.removeFirst();
	    // make sure step can be applied
	    if (canApply(obj)) {
		apply(obj, g);
		System.out.println(this + "(" + obj + ") --> " + g);
		return true;
	    }
	}
	return false;
    }

    /**
     * initializes the FCRStep as part of an FCRAlgorithm
     * computes the initially applicable transformations
     */
    public abstract void initApply();

    /**
     * applies a single transformation to the FCRObject provided by the FCRAlgorithm
     * @param obj the arc/vertex the step is tried to be applied to
     */
    protected abstract void apply(Object obj, ContractGraph g);

    /**
     * checks that obj is still in graph, then calls canApply(obj, g)
     * @return true iff obj is in the graph and the contraction step can be applied
     */
    protected boolean canApply(Object obj) {
	ContractGraph g = (ContractGraph)alg.getObject();
	if (!g.containsVertex(obj) && !(obj instanceof Edge && g.containsEdge((Edge)obj)))
	    return false;
	else
	    return canApply(obj, g);
    }

    /**
     * @return true iff the contraction step can be applied - obj must be in graph
     */
    protected abstract boolean canApply(Object obj, ContractGraph g);

    /**
     * called by update - provides the update parameters in 'nicer' form
     */
    protected abstract void updateEvent(Object obj, Object event);
    
    /**
     * calls UpdateEvent whenever an update occurs
     * @param arg must be of type UpdateGraph.UpdateData
     */
    public void update(Observable o, Object arg) {
	UpdateGraph.UpdateData data = (UpdateGraph.UpdateData)arg;
	updateEvent(data.obj, data.event);
	//System.out.println("updated " + this + ": " + data.obj);
    }

    /**
     * stores an arc/vertex for later application of the step
     */
    protected void store(Object obj) {
	// check applicability here to save memory
	if (canApply(obj))
	    applications.add(obj);
    }

    public String toString() {
	return getClass().getName().replaceFirst(".*\\$","");
    }

}
