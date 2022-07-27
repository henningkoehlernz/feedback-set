package feedback;

import java.util.*;

import grasp.*;

/**
 * vertex class for use in the FVS problem for directed weighted graphs
 */
public class FeedbackVertex implements GreedyObject {

    public static final double infinity = -1;

    private Set id;
    private double weight;

    public FeedbackVertex() {
    }

    public FeedbackVertex(double weight, int id) {
	this.weight = weight;
	this.id = new HashSet();
	this.id.add(new Integer(id));
    }
	
    public FeedbackVertex(double weight, Set id) {
	this.weight = weight;
	this.id = id;
    }

    public Set getID() {
	return id;
    }

    private void setID(Set id) {
	this.id = id;
    }

    public double getWeight() {
	return weight;
    }

    public void setWeight(double weight) {
	this.weight = weight;
    }

    public boolean smallerEqual(double weight) {
	if (weight == infinity)
	    return true;
	if (this.weight == infinity)
	    return false;
	return this.weight <= weight;
    }

    public boolean smallerEqual(FeedbackVertex vertex) {
	return smallerEqual(vertex.getWeight());
    }

    /**
     * adds the IDs of vertex to its own ID set and adds the weight of vertex to its own
     * NOTE: Does NOT update the ID->vertex mapping in the graph, thus should only be called
     * by the graph the vertex belongs to!
     */
    public void merge(FeedbackVertex vertex) {
	id.addAll(vertex.getID());
	if (weight == infinity || vertex.getWeight() == infinity)
	    weight = infinity;
	else
	    weight += vertex.getWeight();
    }

    public Object clone() {
	Set cloneID = new HashSet();
	cloneID.addAll(id);
	//return new FeedbackVertex(getWeight(), cloneID);
	FeedbackVertex clone;
	try {
	    clone = (FeedbackVertex)getClass().newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	clone.setWeight(getWeight());
	clone.setID(cloneID);
	return clone;
    }
    
    public String toString() {
	if (getWeight() == 1)
	    return "(ID=" + id + ")";
	else
	    return "(ID=" + id + /*":W=" + getWeight() +*/ ")";
    }

    // implements GreedyObject interface
    private FeedbackVertexGraph graph = null;

    public void setGraph(FeedbackVertexGraph graph) {
	this.graph = graph;
    }

    public double getGreedyWeight() {
	if (graph == null)
	    return 1/getWeight();
	else
	    return Math.sqrt(graph.inDegreeOf(this) * graph.outDegreeOf(this)) / getWeight();
    }
}
