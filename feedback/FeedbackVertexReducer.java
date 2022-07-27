package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

public class FeedbackVertexReducer {

    public static final int reduceSimple = 0;
    public static final int reduceUpdate = 1;
    public static final int reduceFull   = 2;

    private int reductionMethod;

    public FeedbackVertexReducer() {
	this(reduceSimple);
    }

    public FeedbackVertexReducer(int reductionMethod) {
	this.reductionMethod = reductionMethod;
    }

    /**
     * removes all vertices that have zero in- or out-degree
     * @return number of vertices removed
     */
    /*
    protected int reduce0(FeedbackVertexGraph g) {
	LinkedList removals = new LinkedList();
	Iterator it = g.vertexSet().iterator();
	int removed = 0;
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
	    removed++;
	}
	return removed;
    }
    */

    /**
     * eliminates vertices with inDegree == 1 or outDegree == 1 if proper weights
     * IDs of vertices removed are added to fvs
     * @return number of vertices eliminated
     */
    /*
    protected int reduce1(FeedbackVertexGraph g, Set fvs) {

	int removed = 0;
	boolean oneVertexFound;

	do {
	    oneVertexFound = false;
	    Object[] vertices = g.vertexSet().toArray();
	    for (int i = 0; i < vertices.length; i++) {
		FeedbackVertex cover = null;
		// candidate?
		if (g.inDegreeOf(vertices[i]) == 1) {
		    cover = (FeedbackVertex)((Edge)g.incomingEdgesOf(vertices[i]).iterator().next()).getSource();
		    // really a cover vertex?
		    if (!cover.smallerEqual((FeedbackVertex)vertices[i]))
			cover = null;
		}
		// if we haven't found an incoming cover, look for outgoing one
		if (cover == null && g.outDegreeOf(vertices[i]) == 1) {
		    cover = (FeedbackVertex)((Edge)g.outgoingEdgesOf(vertices[i]).iterator().next()).getTarget();
		    // really a cover vertex?
		    if (!cover.smallerEqual((FeedbackVertex)vertices[i]))
			cover = null;
		}
		if (cover == null)
		    continue;
		oneVertexFound = true;

		// anti-parallel edge which would create loop?
		if (g.containsEdge(vertices[i], cover) && g.containsEdge(cover, vertices[i])) {
		    fvs.addAll(cover.getID());
		    g.removeVertex(vertices[i]);
		    g.removeVertex(cover);
		    removed += 2;
		    break;
		}

		// ok, we got a cover vertex: infinity-remove vertex
		g.removeInfinityVertex(vertices[i]);
		removed += 1;
	    }
	} while (oneVertexFound);

	return removed;
    }
    */

    /**
     * checks if a vertex can be 0-reduced and does so if possible
     * @return true if removed
     */
    protected boolean reduce0(FeedbackVertex vertex, FeedbackVertexGraph g) {
	if (!g.containsVertex(vertex) ||
	    g.inDegreeOf(vertex) > 0 && g.outDegreeOf(vertex) > 0)
	    return false;

	g.removeVertex(vertex);
	return true;
    }

    /**
     * checks if a vertex can be 1-reduced and does so if possible
     * IDs of vertices removed are added to fvs
     * @return true if removed
     */
    protected boolean reduce1(FeedbackVertex vertex, FeedbackVertexGraph g, Set fvs) {
	if (!g.containsVertex(vertex))
	    return false;

	FeedbackVertex cover = null;
	if (g.inDegreeOf(vertex) == 1) {
	    cover = (FeedbackVertex)((Edge)g.incomingEdgesOf(vertex).iterator().next()).getSource();
	    // really a cover vertex?
	    if (!cover.smallerEqual((FeedbackVertex)vertex))
		cover = null;
	}
	// if we haven't found an incoming cover, look for outgoing one
	if (cover == null && g.outDegreeOf(vertex) == 1) {
	    cover = (FeedbackVertex)((Edge)g.outgoingEdgesOf(vertex).iterator().next()).getTarget();
	    // really a cover vertex?
	    if (!cover.smallerEqual((FeedbackVertex)vertex))
		cover = null;
	}
	if (cover == null)
	    return false;

	// anti-parallel edge which would create loop?
	if (g.containsEdge(vertex, cover) && g.containsEdge(cover, vertex)) {
	    fvs.addAll(cover.getID());
	    g.removeVertex(vertex);
	    g.removeVertex(cover);
	}
	else
	    // ok, we got a cover vertex: infinity-remove vertex
	    g.removeInfinityVertex(vertex);
	
	return true;
    }

    /**
     * checks if a vertex can be 0- or 1-reduced and does so if possible
     * IDs of vertices removed are added to fvs
     * @return true if removed
     */
    protected boolean reduceCoveredVertex(FeedbackVertex vertex, FeedbackVertexGraph g, Set fvs) {
	return reduce0(vertex, g) || reduce1(vertex, g, fvs);
    }

    /**
     * merges all parallel vertices
     * @return number of vertices eliminated
     */
    protected int reduceParallel(FeedbackVertexGraph g) {
	int reduced = 0;
	// find parallel vertices
	Map parallel = new ParallelVertexFinder().findParallelVertices(g);
	// merge them
	FeedbackVertex[] vertices = (FeedbackVertex[])g.vertexSet().toArray(new FeedbackVertex[0]);
	for (int i = 0; i < vertices.length; i++) {
	    // has parallel set already been handled?
	    if (!g.containsVertex(vertices[i]))
		continue;
	    Iterator it = ((Set)parallel.get(vertices[i])).iterator();
	    // merge all parallel vertices with vertices[i]
	    while (it.hasNext()) {
		Object vertex = it.next();
		if (vertex != vertices[i]) {
		    //g.removeVertex(vertex);
		    //vertices[i].merge((FeedbackVertex)vertex);
		    g.mergeVertices(vertices[i], (FeedbackVertex)vertex);
		    reduced++;
		}
	    }
	}
	return reduced;
    }

    /**
     * reduces g by eliminating completely direct-connected edges
     */
    /*
    protected int reduceEdges(FeedbackVertexGraph g) {
	int reduced = 0;
	boolean nowReduced;
	do {
	    nowReduced = false;
	    FVSEdge[] edges = (FVSEdge[])g.edgeSet().toArray(new FVSEdge[0]);
	    for (int i = 0; i < edges.length; i++)
		if (edges[i].getPreConnect() == g.inDegreeOf(edges[i].getSource()) ||
		    edges[i].getSuccConnect() == g.outDegreeOf(edges[i].getTarget())) {
		    g.removeEdge(edges[i]);
		    reduced++;
		    nowReduced = true;
		}
	} while (nowReduced);

	return reduced;
    }
    */

    protected boolean reduceEdge(FVSEdge edge, FeedbackVertexGraph g) {
	if (!g.containsEdge(edge) ||
	    edge.getPreConnect() > 0 && edge.getSuccConnect() > 0)
	    return false;

	return g.removeEdge(edge);
    }

    protected boolean reduceEdges(FeedbackVertexGraph g) {
	boolean reduced = false;
	while (!g.zeroEdges.isEmpty())
	    if (reduceEdge((FVSEdge)g.zeroEdges.removeAny(), g))
		reduced = true;
	return reduced;
    }

    
    /**
     * reduces g by eliminating 0-vertices
     * @param fvs feedback vertex set - gets updated during reduction
     */
    public boolean reduceBy0(FeedbackVertexGraph g) {
	boolean reduced = false;
	while (!g.reductionVertices.isEmpty())
	    if (reduce0((FeedbackVertex)g.reductionVertices.removeAny(), g))
		reduced = true;
	return reduced;
    }

    /**
     * reduces g by eliminating 0-vertices and 1-vertices
     * @param fvs feedback vertex set - gets updated during reduction
     */
    public boolean reduceSimple(FeedbackVertexGraph g, Set fvs) {
	boolean reduced = false;
	while (!g.reductionVertices.isEmpty())
	    if (reduceCoveredVertex((FeedbackVertex)g.reductionVertices.removeAny(), g, fvs))
		reduced = true;
	return reduced;
    }

    /**
     * reduces g by eliminating 0-vertices and 1-vertices
     * @param fvs feedback vertex set - gets updated during reduction
     */
    public boolean reduceUpdate(FeedbackVertexGraph g, Set fvs) {
	boolean reduced = false;
	while (!g.reductionVertices.isEmpty() || !g.zeroEdges.isEmpty()) {
	    while (!g.reductionVertices.isEmpty())
		if (reduceCoveredVertex((FeedbackVertex)g.reductionVertices.removeAny(), g, fvs))
		    reduced = true;
	    while (!g.zeroEdges.isEmpty())
		if (reduceEdge((FVSEdge)g.zeroEdges.removeAny(), g))
		    reduced = true;
	}
	return reduced;
    }

    /**
     * reduces g by eliminating 0-vertices and 1-vertices, merging parallel vertices and removing edges
     * @param fvs feedback vertex set - gets updated during reduction
     */
    public boolean reduceFull(FeedbackVertexGraph g, Set fvs) {
	boolean reduced = false;
	int lastReduceID = -1;
	int reduceID = -1;
	
	while (true) {
	    // increase mod 2
	    reduceID++;
	    if (reduceID >= 2) {
		// no reduction possible at all?
		if (lastReduceID == -1)
		    return false;
		reduceID -= 2;
	    }
	    // no improvement since last check?
	    if (lastReduceID == reduceID)
		return true;

	    // reduce
	    switch (reduceID) {
	    case 0:
		reduced = reduceUpdate(g, fvs);
		break;
	    case 1:
		reduced = reduceParallel(g) > 0;
		break;
	    default: throw new RuntimeException("unexpected ID");
	    }
	    if (reduced)
		lastReduceID = reduceID;
	}
    }

    public void reduce(FeedbackVertexGraph g, Set fvs) {
	switch (reductionMethod) {
	case reduceSimple:
	    reduceSimple(g, fvs);
	    break;
	case reduceUpdate:
	    reduceUpdate(g, fvs);
	    break;
	case reduceFull:
	    reduceFull(g, fvs);
	    break;
	default:
	    throw new RuntimeException("Unknown reduction method: " + reductionMethod);
	}
    }

}
