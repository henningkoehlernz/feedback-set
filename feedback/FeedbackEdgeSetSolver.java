package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;
import org._3pq.jgrapht.traverse.*;

import util.Log;

public class FeedbackEdgeSetSolver {

    protected class GreedyEdge extends DirectedWeightedEdge {
	private Set id;
	private DirectedGraph graph;
	
	public GreedyEdge(Object sourceVertex, Object targetVertex, double weight,
			  Set id, DirectedGraph g) {
	    super(sourceVertex, targetVertex, weight);
	    this.id = id;
	    this.graph = g;
	}

	public Set getID() {
	    return id;
	}

	public void mergeWithEdge(GreedyEdge edge) {
	    if (edge.getSource() != getSource() || edge.getTarget() != getTarget())
		throw new IllegalArgumentException("attempting to merge with different edge");
	    id.addAll(edge.getID());
	    setWeight(getWeight() + edge.getWeight());
	}

	public double getGreedyWeight() {
	    int inDegree = graph.inDegreeOf(getSource());
	    int outDegree = graph.outDegreeOf(getTarget());
	    return Math.sqrt(inDegree * outDegree) / getWeight();
	}

	public GreedyEdge clone(DirectedGraph newGraph) {
	    Set cloneID = new HashSet();
	    cloneID.addAll(id);
	    return new GreedyEdge(getSource(), getTarget(), getWeight(), cloneID, newGraph);
	}

	public String toString() {
	    return super.toString() + ":" + getWeight() + "=" + id;
	}
    }

    protected class GreedyGraph extends SimpleDirectedWeightedGraph {
	
	// maps IDs to edges
	private HashMap idEdges = new HashMap();

	public boolean removeEdge(Edge edge) {
	    if (!super.removeEdge(edge))
		return false;
	    // remove IDs from idEdges
	    Iterator it = ((GreedyEdge)edge).getID().iterator();
	    while (it.hasNext())
		idEdges.remove(it.next());
	    return true;
	}

	public boolean addEdge(Edge edge) {
	    // make sure the graph contains no edges with same ID
	    Iterator it = ((GreedyEdge)edge).getID().iterator();
	    while (it.hasNext()) {
		Object id = it.next();
		if (idEdges.containsKey(id))
		    throw new IllegalArgumentException("Graph already contains edge with id=" +  id);
	    }
	    // already contains edge from source to target?
	    GreedyEdge oldEdge = (GreedyEdge)getEdge(edge.getSource(), edge.getTarget());
	    GreedyEdge newEdge;
	    if (oldEdge != null) {
		oldEdge.mergeWithEdge((GreedyEdge)edge);
		newEdge = oldEdge;
	    }
	    else {
		if (!super.addEdge(edge))
		    return false;
		newEdge = (GreedyEdge)edge;
	    }
	    // update idEdges
	    it = ((GreedyEdge)edge).getID().iterator();
	    while (it.hasNext())
		idEdges.put(it.next(), newEdge);
	    return true;
	}

	public Edge addEdge(Object sourceVertex, Object targetVertex,
			    double weight, int id) {
	    Set idSet = new HashSet();
	    idSet.add(new Integer(id));
	    return addEdge(sourceVertex, targetVertex, weight, idSet);
	}

	public Edge addEdge(Object sourceVertex, Object targetVertex,
			    double weight, Set idSet) {
	    GreedyEdge edge = new GreedyEdge(sourceVertex, targetVertex, weight, idSet, this);
	    addEdge(edge);
	    return edge;
	}

	public Edge addEdge(DirectedEdge baseEdge, int id) {
	    double weight = 1;
	    if (baseEdge instanceof DirectedWeightedEdge)
		weight = ((DirectedWeightedEdge)baseEdge).getWeight();
	    return addEdge(baseEdge.getSource(), baseEdge.getTarget(), weight, id);
	}

	public GreedyEdge getEdge(Integer id) {
	    return (GreedyEdge)idEdges.get(id);
	}

	public Set getEdges(Set idSet) {
	    Set edges = new HashSet();
	    Iterator it = idSet.iterator();
	    while (it.hasNext())
		edges.add(getEdge((Integer)it.next()));
	    return edges;
	}

	public Object clone() {
	    GreedyGraph clone = new GreedyGraph();
	    Iterator it = vertexSet().iterator();
	    while (it.hasNext())
		clone.addVertex(it.next());
	    it = edgeSet().iterator();
	    while (it.hasNext())
		clone.addEdge(((GreedyEdge)it.next()).clone(clone));
	    return clone;
	}

	public int degreeOf(Object vertex) {
	    return inDegreeOf(vertex) + outDegreeOf(vertex);
	}

	/**
	 * @return weight of the FES
	 * @param fes must contain Integer IDs
	 */
	public double getFESWeight(Set fes) {
	    // make sure edges with multiple IDs get counted only once
	    Set counted = new HashSet();
	    double weight = 0;
	    Iterator it = fes.iterator();
	    while (it.hasNext()) {
		GreedyEdge edge = getEdge((Integer)it.next());
		if (!counted.contains(edge)) {
		    weight += edge.getWeight();
		    counted.add(edge);
		}
	    }
	    return weight;
	}
   }

    protected int iterations;
    protected double rclFactor;

    /*
    public FeedbackEdgesSetSolver() {
	this(100, 0.8);
    }

    public FeedbackSets(int iterations) {
	this(iterations, 0.8);
    }

    public FeedbackSets(int iterations, double rclFactor) {
	this.iterations = iterations;
	this.rclFactor = rclFactor;
    }
    */

    /**
     * finds FES for g using GRASP
     * @param g can be weighted but must be loop-free, if weighted all weights must be > 0
     */
    public Set findGreedyFeedbackEdgeSet(DirectedGraph g) {

	// create ids for edges
	DirectedEdge[] edgeIndex = (DirectedEdge[])g.edgeSet().toArray(new DirectedEdge[0]);

	// produce copy with GreedyEdges
	GreedyGraph idGraph = new GreedyGraph();
	Iterator it = g.vertexSet().iterator();
	while (it.hasNext())
	    idGraph.addVertex(it.next());
	for (int i = 0; i < edgeIndex.length; i++)
	    idGraph.addEdge(edgeIndex[i], i);

	// reduce once before iterations
	Set mainFES = new HashSet();
	reduce(idGraph, mainFES);
	// is graph already empty?
	if (!idGraph.edgeSet().isEmpty()) {
	    // iterate
	    Set minFES = null;
	    double minWeight = 0;
	    for (int i = 0; i < iterations; i++) {
		Set newFES = iterateFES(idGraph);
		double newWeight = idGraph.getFESWeight(newFES);
		// Log.debug("FESweight=" + newWeight);
		// better result than old one?
		if (minFES == null || newWeight < minWeight) {
		    minFES = newFES;
		    minWeight = newWeight;
		}
	    }
	    mainFES.addAll(minFES);
	}

	// convert back
	Set returnFES = new HashSet();
	it = mainFES.iterator();
	while (it.hasNext())
	    returnFES.add(edgeIndex[((Integer)it.next()).intValue()]);

	return returnFES;
    }

    private Random rand = new Random();
    /**
     * @return FES from an already reduced graph
     */
    protected Set iterateFES(GreedyGraph g) {
	// ensure that g is not modified
	GreedyGraph itGraph = (GreedyGraph)g.clone();
	Set fes = new HashSet();
	while (!itGraph.edgeSet().isEmpty()) {
	    // randomly select edge to remove from rcl
	    GreedyEdge[] rcl = (GreedyEdge[])getRCL(itGraph).toArray(new GreedyEdge[0]);
	    GreedyEdge edge = rcl[rand.nextInt(rcl.length)];
	    // remove and reduce
	    itGraph.removeEdge(edge);
	    fes.addAll(edge.getID());
	    reduce(itGraph, fes);
	}
	// remove redundant edges from fes
	localSearch(g, fes);
	return fes;
    }

    /**
     * removes all vertices that have zero in- or out-degree
     */
    protected void reduce0(GreedyGraph g) {
	LinkedList removals = new LinkedList();
	Iterator it = g.vertexSet().iterator();
	// find starting set for removals
	while (it.hasNext()) {
	    Object vertex = it.next();
	    if (g.inDegreeOf(vertex) == 0 || g.outDegreeOf(vertex) == 0)
		removals.add(vertex);
	}
	// remove..
	while (!removals.isEmpty()) {
	    Object vertex = removals.removeFirst();
	    // update adjecent vertices and remove
	    // update predecessors
	    it = g.incomingEdgesOf(vertex).iterator();
	    while (it.hasNext()) {
		Object pre = ((DirectedEdge)it.next()).getSource();
		// if pre newly becomes subject of removal, add to list
		if (g.outDegreeOf(pre) == 1 && g.inDegreeOf(pre) > 0)
		    removals.add(pre);
	    }
	    // update successors
	    it = g.outgoingEdgesOf(vertex).iterator();
	    while (it.hasNext()) {
		Object succ = ((DirectedEdge)it.next()).getTarget();
		// if succ newly becomes subject of removal, add to list
		if (g.inDegreeOf(succ) == 1 && g.outDegreeOf(succ) > 0)
		    removals.add(succ);
	    }
	    g.removeVertex(vertex);
	}
    }

    /**
     * eliminate vertices with inDegree == outDegree == 1
     * @return true if graph again contains zero-Vertices
     */
    protected boolean reduce1(GreedyGraph g, Set fes) {

	//Log.debug("g = " + g);
	//Log.debug("fes = " + fes + "\nreducing..");

	boolean zeroVertexFound = false;
	boolean oneVertexFound;
	do {
	    oneVertexFound = false;
	    Object[] vertices = g.vertexSet().toArray();
	    for (int i = 0; i < vertices.length; i++)
		if (g.inDegreeOf(vertices[i]) == 1 && g.outDegreeOf(vertices[i]) == 1) {
		    GreedyEdge e1 = (GreedyEdge)g.incomingEdgesOf(vertices[i]).iterator().next();
		    GreedyEdge e2 = (GreedyEdge)g.outgoingEdgesOf(vertices[i]).iterator().next();
		    Object v1 = e1.getSource();
		    Object v2 = e2.getTarget();
		    GreedyEdge lightEdge;
		    // which edge is lighter and should be kept/fes-removed?
		    if (e1.getWeight() <= e2.getWeight())
			lightEdge = e1;
		    else
			lightEdge = e2;
		    // remove before adding new edge with same ID
		    g.removeVertex(vertices[i]);
		    // make sure not to create loop..
		    if (v1 == v2) {
			fes.addAll(lightEdge.getID());
			// did removal create zero-vertex?
			if (g.inDegreeOf(v1) == 0 || g.outDegreeOf(v1) == 0)
			    zeroVertexFound = true;
		    }
		    else {
			// we might have created a new one-vertex..
			if (g.containsEdge(v1, v2)
			    && (g.inDegreeOf(v1) == 1 && g.outDegreeOf(v1) == 1
				|| g.inDegreeOf(v2) == 1 && g.outDegreeOf(v2) == 1))
			    oneVertexFound = true;
			g.addEdge(v1, v2, lightEdge.getWeight(), lightEdge.getID());
		    }
		}
	} while (oneVertexFound);
	//Log.debug("g = " + g);
	//Log.debug("fes = " + fes);
	return zeroVertexFound;
    }

    /**
     * reduces g for FES calculation
     * @param fes the FES - might get increased
     */
    protected void reduce(GreedyGraph g, Set fes) {
	do
	    reduce0(g);
	while (reduce1(g, fes));
    }

    /**
     * @return the restricted candidate list of g
     */
    protected Set getRCL(GreedyGraph g) {
	// find max value
	GreedyEdge[] edges = (GreedyEdge[])g.edgeSet().toArray(new GreedyEdge[0]);
	double[] greedyWeights = new double[edges.length];
	double maxGreed = 0;
	for (int i = 0; i < edges.length; i++) {
	    greedyWeights[i] = edges[i].getGreedyWeight();
	    if (greedyWeights[i] > maxGreed)
		maxGreed = greedyWeights[i];
	}
	// construct rcl
	Set rcl = new HashSet();
	double border = maxGreed * rclFactor;
	for (int i = 0; i < edges.length; i++)
	    if (greedyWeights[i] >= border)
		rcl.add(edges[i]);
	return rcl;
    }

    protected void localSearch(GreedyGraph g, Set fes) {
	// construct g\fes:
	GreedyGraph acyclic = (GreedyGraph)g.clone();
	GreedyEdge[] edges = (GreedyEdge[])acyclic.getEdges(fes).toArray(new GreedyEdge[0]);
	for (int i = 0; i < edges.length; i++)
	    acyclic.removeEdge(edges[i]);
	if (!isAcyclic(acyclic)) {
	    throw new RuntimeException("Graph not acyclic: " + acyclic + " = " + g + " - " + fes);
	}
	// try to add edges back
	for (int i = 0; i < edges.length; i++)
	    if (!pathExists(acyclic, edges[i].getTarget(), edges[i].getSource())) {
		acyclic.addEdge(edges[i]);
		fes.removeAll(edges[i].getID());
	    }
	if (!isAcyclic(acyclic))
	    throw new RuntimeException("Graph made cyclic: " + acyclic);
    }

    protected boolean isAcyclic(GreedyGraph g) {
	GreedyGraph clone = (GreedyGraph)g.clone();
	reduce0(clone);
	return clone.edgeSet().isEmpty();
    }

    protected boolean pathExists(Graph g, Object sourceVertex, Object targetVertex) {
	Iterator it = new BreadthFirstIterator(g, sourceVertex);
	while (it.hasNext())
	    if (it.next() == targetVertex)
		return true;
	return false;
    }

}
