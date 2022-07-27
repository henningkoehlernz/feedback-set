package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import grasp.*;

public class FVSProblem implements GRASPProblem {

    private FeedbackVertexGraph originalGraph, graph;
    private FeedbackVertexReducer reducer;
    // stores IDs of vertices
    private HashSet fvs = new HashSet();

    /**
     * for cloning use only, don't call!
     */
    public FVSProblem() {
    }

    public FVSProblem(FeedbackVertexGraph graph) {
	this(graph, new FeedbackVertexReducer());
    }

    public FVSProblem(FeedbackVertexGraph graph, FeedbackVertexReducer reducer) {
	this.originalGraph = graph;
	this.graph = (FeedbackVertexGraph)originalGraph.clone();
	this.reducer = reducer;
    }

    /**
     * @return the objects available for greedy-selection
     */
    public Set getGreedyObjects() {
	return graph.vertexSet();
    }

    public void reduce() {
	reducer.reduce(graph, fvs);
    };

    /**
     * informs the GRASP-problem of a selection
     * @param obj is one of the elements from getGreedyObjects
     */
    public void selectGreedyObject(GreedyObject obj) {
	FeedbackVertex vertex = (FeedbackVertex)obj;
	fvs.addAll(vertex.getID());
	graph.removeVertex(vertex);
	reduce();
    }

    /**
     * performs local optimization
     */
    public void optimizeLocal() {
	int reduced = 0;
	// try to remove redundant vertices
	HashSet cloneFVS = (HashSet)fvs.clone();
	Iterator it = fvs.iterator();
	while (it.hasNext()) {
	    Object vertexID = it.next();
	    // check if vertex is redundant
	    cloneFVS.remove(vertexID);
	    if (!isFVS(cloneFVS))
		cloneFVS.add(vertexID);
	    else
		reduced++;
	}
	fvs = cloneFVS;
	//System.out.print(reduced);
    }

    /**
     * @return true if fvs is a Feedback Vertex Set
     * @param fvs set of vertex IDs
     */
    protected boolean isFVS(Set fvs) {
	FeedbackVertexGraph g = (FeedbackVertexGraph)originalGraph.clone();
	// remove vertices in fvs
	Iterator it = fvs.iterator();
	while (it.hasNext()) {
	    g.removeVertex(g.getVertex(it.next()));
	}
	// check if cycle-free
	reducer.reduceBy0(g);
	return g.vertexSet().isEmpty();
    }

    /**
     * will only be called when getGreedyObjects() is empty
     * @return a weight for the solution found; lower = better
     */
    public double getSolutionWeight() {
	return getSolutionWeight(getSolution());
    }
    /**
     * @return the weight of a given solution
     */
    public double getSolutionWeight(Object solution) {
	// init mapping ID -> weight; store weight only for first ID
	HashMap weightMap = new HashMap();
	Iterator it = originalGraph.vertexSet().iterator();
	while (it.hasNext()) {
	    FeedbackVertex vertex = (FeedbackVertex)it.next();
	    weightMap.put(vertex.getID().iterator().next(), new Double(vertex.getWeight()));
	}
	// sum up weight
	double weight = 0;
	it = ((Set)solution).iterator();
	while (it.hasNext()) {
	    Object id = it.next();
	    if (weightMap.containsKey(id))
		weight += ((Double)weightMap.get(id)).doubleValue();
	}

	return weight;
    }

    /**
     * @return the solution found
     */
    public Object getSolution() {
	return fvs;
    }

    /**
     * @return a deep-copy of itself
     */
    public Object clone() {
	FVSProblem clone;
	try {
	    clone = (FVSProblem)super.clone();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	clone.originalGraph = originalGraph;
	clone.graph = (FeedbackVertexGraph)originalGraph.clone();
	clone.reducer = reducer;
	clone.fvs = (HashSet)fvs.clone();
	return clone;
    }
}
