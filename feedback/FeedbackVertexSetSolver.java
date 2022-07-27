package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;
import org._3pq.jgrapht.traverse.*;

import util.Log;

public class FeedbackVertexSetSolver {

    protected class GreedyVertex extends FeedbackVertex {
	private DirectedGraph graph;
	
	public GreedyVertex(double weight, int id, DirectedGraph g) {
	    super(weight, id);
	    this.graph = g;
	}

	public GreedyVertex(double weight, Set id, DirectedGraph g) {
	    super(weight, id);
	    this.graph = g;
	}

	public double getGreedyWeight() {
	    int inDegree = graph.inDegreeOf(this);
	    int outDegree = graph.outDegreeOf(this);
	    return Math.sqrt(inDegree * outDegree) / getWeight();
	}
    }

    protected class GreedyGraph extends FeedbackVertexGraph {
	
	// maps IDs to vertices
	private HashMap idVertices = new HashMap();

	public boolean removeVertex(Object vertex) {
	    if (!super.removeVertex(vertex))
		return false;
	    // remove IDs from idVertices
	    Iterator it = ((GreedyVertex)vertex).getID().iterator();
	    while (it.hasNext())
		idVertices.remove(it.next());
	    return true;
	}

	public boolean addVertex(Object vertex) {
	    // make sure the graph contains no vertices with same ID
	    Iterator it = ((GreedyVertex)edge).getID().iterator();
	    while (it.hasNext()) {
		Object id = it.next();
		if (idVertices.containsKey(id))
		    throw new IllegalArgumentException("Graph already contains vertex with id=" +  id);
	    }
	    // add..
	    if (!super.addVertex(vertex))
		return false;
	    // update idVertices
	    it = ((GreedyVertex)vertex).getID().iterator();
	    while (it.hasNext())
		idVertices.put(it.next(), vertex);
	    return true;
	}

	public GreedyVertex getVertex(Integer id) {
	    return (GreedyVertex)idVertices.get(id);
	}

	public Set getVertices(Set idSet) {
	    Set vertices = new HashSet();
	    Iterator it = idSet.iterator();
	    while (it.hasNext())
		vertices.add(getVertex((Integer)it.next()));
	    return vertices;
	}

	public int degreeOf(Object vertex) {
	    return inDegreeOf(vertex) + outDegreeOf(vertex);
	}

	/**
	 * @return weight of the FVS
	 * @param fvs must contain Integer IDs
	 */
	public double getFVSWeight(Set fvs) {
	    // make sure edges with multiple IDs get counted only once
	    Set counted = new HashSet();
	    double weight = 0;
	    Iterator it = fvs.iterator();
	    while (it.hasNext()) {
		GreedyVertex vertex = getVertex((Integer)it.next());
		if (!counted.contains(vertex)) {
		    weight += vertex.getWeight();
		    counted.add(vertex);
		}
	    }
	    return weight;
	}
   }

    protected int iterations;
    protected double rclFactor;

    public FeedbackVertexSetSolver() {
	this(100, 0.8);
    }

    public FeedbackVertexSetSolver(int iterations) {
	this(iterations, 0.8);
    }

    public FeedbackVertexSetSolver(int iterations, double rclFactor) {
	this.iterations = iterations;
	this.rclFactor = rclFactor;
    }

    /**
     * finds FVS for g using GRASP
     * @param g can be weighted, if weighted all weights must be > 0
     */
    public Set findGreedyFeedbackVertexSet(SimpleDirectedGraph g) {

	// create ids for edges
	Object[] vertexIndex = g.vertexSet().toArray();
	// maps old vertices to new ones
	HashMap vertexMap = new HashMap();

	// produce copy with GreedyVertices
	GreedyGraph idGraph = new GreedyGraph();
	for (int i = 0; i < vertexIndex.length; i++) {
	    double weight = 1;
	    if (vertexIndex[i] instanceof FeedbackVertex)
		weight = ((FeedbackVertex)vertexIndex[i]).getWeight();
	    GreedyVertex newVertex = new GreedyVertex(weight, vertexIndex[i], idGraph);
	    vertexMap.put(vertexIndex[i], newVertex);
	    idGraph.addVertex(newVertex);
	}
	Iterator it = g.edgeSet().iterator();
	while (it.hasNext()) {
	    edge = (Edge)it.next();
	    idGraph.addEdge(vertexMap.get(edge.getSource()), vertexMap.get(edge.getTarget()));
	}

	// reduce once before iterations .. continue to edit here..
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
