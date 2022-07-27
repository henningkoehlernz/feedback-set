package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import util.*;

public class FeedbackVertexGraph extends SimpleDirectedGraph {

    private boolean edgeConnectInitialized = false;
    private HashMap vertexIDMap = new HashMap();

    // zero- and one-vertices and zero-edges
    public final AnySet reductionVertices = new AnySet();
    public final AnySet zeroEdges = new AnySet();

    public FeedbackVertexGraph() {
	super(new EdgeFactory() {
		public Edge createEdge(Object source, Object target) {
		    return new FVSEdge(source, target);
		}
	    });
    }

    public boolean addVertex(Object vertex) {
	if (vertex instanceof FeedbackVertex) {
	    ((FeedbackVertex)vertex).setGraph(this);
	    // update vertexIDMap
	    Iterator it = ((FeedbackVertex)vertex).getID().iterator();
	    while (it.hasNext())
		vertexIDMap.put(it.next(), vertex);
	}
	return super.addVertex(vertex);
    }

    public boolean removeVertex(Object vertex) {
	// update vertexIDMap
	if (vertex instanceof FeedbackVertex) {
	    Iterator it = ((FeedbackVertex)vertex).getID().iterator();
	    while (it.hasNext())
		vertexIDMap.remove(it.next());
	}
	return super.removeVertex(vertex);
    }

    /**
     * @return the vertex that corresponds to the given ID
     */
    public Object getVertex(Object id) {
	return vertexIDMap.get(id);
    }

    /**
     * removes vertex v1 and adds an edge from each predecessor to each successor
     * @return number of edges newly added
     */
    public int removeInfinityVertex(Object v1) {
	int added = 0;
	Edge[] inEdges = (Edge[])incomingEdgesOf(v1).toArray(new Edge[0]);
	Edge[] outEdges = (Edge[])outgoingEdgesOf(v1).toArray(new Edge[0]);
	removeVertex(v1);
	for (int i = 0; i < inEdges.length; i++) {
	    Object source = inEdges[i].getSource();
	    for (int j = 0; j < outEdges.length; j++) {
		Object target = outEdges[j].getTarget();
		if (!containsEdge(source, target)) {
		    addEdge(source, target);
		    added++;
		}
	    }
	}
	return added;
    }

    /**
     * merges v1 and v2 via v1.merge(v2) into a single vertex
     * does update the mapping ID->vertex
     */
    public void mergeVertices(FeedbackVertex v1, FeedbackVertex v2) {
	if (v1 == v2)
	    return;
	removeVertex(v2);
	v1.merge(v2);
	// update mapping ID->vertex
	Iterator it = v2.getID().iterator();
	while (it.hasNext())
	    vertexIDMap.put(it.next(), v1);
    }

    /**
     * removes the given edge from the graph and updates direct connect numbers
     */
    public boolean removeEdge(Edge edge) {
	Object source = edge.getSource();
	Object target = edge.getTarget();

	// update edges
	if (edgeConnectInitialized) {
	    try {
	    // update direct connects where edge is second edge
	    FVSEdge[] sourceInEdges = (FVSEdge[])incomingEdgesOf(source).toArray(new FVSEdge[0]);
	    for (int i = 0; i < sourceInEdges.length; i++)
		if (!containsEdge(sourceInEdges[i].getSource(), target))
		    sourceInEdges[i].decSuccConnect(zeroEdges);
	    // update direct connects where edge is first edge
	    FVSEdge[] targetOutEdges = (FVSEdge[])outgoingEdgesOf(target).toArray(new FVSEdge[0]);
	    for (int i = 0; i < targetOutEdges.length; i++)
		if (!containsEdge(source, targetOutEdges[i].getTarget()))
		    targetOutEdges[i].decPreConnect(zeroEdges);
	    // update direct connects where edge is direct edge
	    FVSEdge[] sourceOutEdges = (FVSEdge[])outgoingEdgesOf(source).toArray(new FVSEdge[0]);
	    for (int i = 0; i < sourceOutEdges.length; i++) {
		FVSEdge secondEdge = (FVSEdge)getEdge(sourceOutEdges[i].getTarget(), target);
		if (secondEdge != null) {
		    sourceOutEdges[i].incSuccConnect();
		    secondEdge.incPreConnect();
		}
	    }
	    } catch(RuntimeException e) {
		//e.printStackTrace();
		System.out.println("G = " + this);
		System.out.println("edge = " + edge);
		throw e;
	    }
	}

	// update zero- and one-vertices
	if (outDegreeOf(source) <= 2)
	    reductionVertices.add(source);
	if (inDegreeOf(target) <= 2)
	    reductionVertices.add(target);

	return super.removeEdge(edge);
    }

    /**
     * fix for lousy design of jgraph.. now calls addEdge(Edge)
     */
    public Edge addEdge(Object source, Object target) {
	Edge edge = getEdgeFactory().createEdge(source, target);
	if (addEdge(edge))
	    return edge;
	else
	    return null;
    }

    /**
     * adds given edge to the graph and updates direct connect numbers
     */
    public boolean addEdge(Edge edge) {
	if (edgeConnectInitialized) {
	    try {
	    Object source = edge.getSource();
	    Object target = edge.getTarget();
	    // update direct connects where edge is second edge
	    FVSEdge[] sourceInEdges = (FVSEdge[])incomingEdgesOf(source).toArray(new FVSEdge[0]);
	    for (int i = 0; i < sourceInEdges.length; i++)
		if (!containsEdge(sourceInEdges[i].getSource(), target))
		    sourceInEdges[i].incSuccConnect();
	    // update direct connects where edge is first edge
	    FVSEdge[] targetOutEdges = (FVSEdge[])outgoingEdgesOf(target).toArray(new FVSEdge[0]);
	    for (int i = 0; i < targetOutEdges.length; i++)
		if (!containsEdge(source, targetOutEdges[i].getTarget()))
		    targetOutEdges[i].incPreConnect();
	    // update direct connects where edge is direct edge
	    FVSEdge[] sourceOutEdges = (FVSEdge[])outgoingEdgesOf(source).toArray(new FVSEdge[0]);
	    for (int i = 0; i < sourceOutEdges.length; i++) {
		FVSEdge secondEdge = (FVSEdge)getEdge(sourceOutEdges[i].getTarget(), target);
		if (secondEdge != null) {
		    sourceOutEdges[i].decSuccConnect(zeroEdges);
		    secondEdge.decPreConnect(zeroEdges);
		}
	    }
	    initEdge((FVSEdge)edge);
	    } catch(RuntimeException e) {
		//e.printStackTrace();
		System.out.println("G = " + this);
		System.out.println("edge = " + edge);
		throw e;
	    }
	}
	return super.addEdge(edge);
    }

    /**
     * inits edge with its direct connect numbers
     */
    public void initEdge(FVSEdge edge) {
	Object source = edge.getSource();
	Object target = edge.getTarget();
	// calculate preConnect number
	int preConnect = 0;
	FVSEdge[] sourceInEdges = (FVSEdge[])incomingEdgesOf(source).toArray(new FVSEdge[0]);
	for (int i = 0; i < sourceInEdges.length; i++)
	    if (!containsEdge(sourceInEdges[i].getSource(), target))
		preConnect++;
	edge.setPreConnect(preConnect, zeroEdges);
	// calculate succConnect number
	int succConnect = 0;
	FVSEdge[] targetOutEdges = (FVSEdge[])outgoingEdgesOf(target).toArray(new FVSEdge[0]);
	for (int i = 0; i < targetOutEdges.length; i++)
	    if (!containsEdge(source, targetOutEdges[i].getTarget()))
		succConnect++;
	edge.setSuccConnect(succConnect, zeroEdges);
    }

    /**
     * inits all edges with direct connect numbers
     */
    public void initEdges() {
	if (edgeConnectInitialized)
	    return;

	Iterator it = edgeSet().iterator();
	while (it.hasNext())
	    initEdge((FVSEdge)it.next());

	edgeConnectInitialized = true;
    }

    /**
     * inits zeroVertices and oneVertices
     */
    public void initReductionVertices() {
	Iterator it = vertexSet().iterator();
	while (it.hasNext()) {
	    Object vertex = it.next();
	    if (inDegreeOf(vertex) <= 1 || outDegreeOf(vertex) <= 1)
		reductionVertices.add(vertex);
	}
    }

    /**
     * inits edges and vertices for reduction
     */
    public void init() {
	initReductionVertices();
	initEdges();
    }

    /**
     * @return a deep-copy of itself: vertices are cloned
     */
    public Object clone() {
	FeedbackVertexGraph clone;
	try {
	    clone = (FeedbackVertexGraph)getClass().newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	HashMap vertexClones = new HashMap();
	// copy vertices
	Iterator it = vertexSet().iterator();
	while (it.hasNext()) {
	    FeedbackVertex vertex = (FeedbackVertex)it.next();
	    Object vertexClone = vertex.clone();
	    vertexClones.put(vertex, vertexClone);
	    clone.addVertex(vertexClone);
	}
	// copy edges
	it = edgeSet().iterator();
	while (it.hasNext()) {
	    Edge edge = (Edge)it.next();
	    Object source = vertexClones.get(edge.getSource());
	    Object target = vertexClones.get(edge.getTarget());
	    clone.addEdge(source, target);
	}
	// init
	clone.initReductionVertices();
	if (edgeConnectInitialized)
	    clone.initEdges();
	return clone;
    }

}
