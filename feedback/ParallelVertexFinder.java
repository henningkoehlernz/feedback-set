package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

/**
 * utility class that identifies parallel vertices in a directed graph
 */
public class ParallelVertexFinder {

    private class SplitSet extends HashSet {

	private SplitSet split = null;
	private int lastSplit = -1;

	/**
	 * moves element o into new SplitSet
	 * @param o element to be moved
	 * @param splitID if splitID != splitID of last call a new split is created
	 * @return the split o got moved into
	 */
	public SplitSet split(Object o, int splitID) {
	    if (!contains(o))
		throw new IllegalArgumentException("Object " + o + " not in set");
	    if (lastSplit != splitID) {
		split = new SplitSet();
		lastSplit = splitID;
	    }
	    remove(o);
	    split.add(o);
	    return split;
	}
    }

    private static Set successorsOf(Object vertex, DirectedGraph g) {
	Set succ = new HashSet();
	Iterator it = g.outgoingEdgesOf(vertex).iterator();
	while (it.hasNext())
	    succ.add(((Edge)it.next()).getTarget());
	return succ;
    }

    private static Set predecessorsOf(Object vertex, DirectedGraph g) {
	Set pre = new HashSet();
	Iterator it = g.incomingEdgesOf(vertex).iterator();
	while (it.hasNext())
	    pre.add(((Edge)it.next()).getSource());
	return pre;
    }

    /**
     * finds parallel vertices in O(|E|)
     * @return mapping: vertex -> set of all parallel vertices
     */
    public Map findParallelVertices(DirectedGraph g) {
	HashMap vertexMap = new HashMap();
	// init vertexMap
	SplitSet mainSplit = new SplitSet();
	mainSplit.addAll(g.vertexSet());
	Iterator it = g.vertexSet().iterator();
	while (it.hasNext())
	    vertexMap.put(it.next(), mainSplit);
	// split by predecessors
	int splitID = 0;
	it = g.vertexSet().iterator();
	while (it.hasNext()) {
	    Iterator splitIt = successorsOf(it.next(), g).iterator();
	    while (splitIt.hasNext()) {
		Object vertex = splitIt.next();
		vertexMap.put(vertex, ((SplitSet)vertexMap.get(vertex)).split(vertex, splitID));
	    }
	    splitID++;
	}
	// split by successors
	it = g.vertexSet().iterator();
	while (it.hasNext()) {
	    Iterator splitIt = predecessorsOf(it.next(), g).iterator();
	    while (splitIt.hasNext()) {
		Object vertex = splitIt.next();
		vertexMap.put(vertex, ((SplitSet)vertexMap.get(vertex)).split(vertex, splitID));
	    }
	    splitID++;
	}
	
	return vertexMap;
    }

}
