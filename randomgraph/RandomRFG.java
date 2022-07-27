package randomgraph;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;


/**
 * class for randomly generating reducible flow graphs
 */
public class RandomRFG {

    /**
     * handles the pre-dominator tree needed for generating backward arcs
     */
    protected class PreDominatorTree {

	private static final int UNDEF = -1;

	// stores the pre-dominators of each vertex
	private int[] preDominator;

	/**
	 * @param size the number of vertices the graph has
	 */
	public PreDominatorTree(int size) {
	    preDominator = new int[size];
	    // set predominators to undefined
	    for (int i = 0; i < size; i++)
		preDominator[i] = UNDEF;
	}
	
	/**
	 * updates the pre-dominator tree to adjust to the added arc
	 * precondition: 0 <= source < target < size
	 * @param source the source vertex of the added arc
	 * @param target the target vertex of the added arc
	 */
	public void update(int source, int target) {
	    if ( !(0 <= source && source < target && target < preDominator.length) )
		throw new IllegalArgumentException("Invalid update arc: " + source + " -> " + target);
	    if (preDominator[target] == UNDEF)
		preDominator[target] = source;
	    else
		preDominator[target] = getMaxDominator(preDominator[target], source);
	}

	/**
	 * @return the maximal common dominator of v1 and v2
	 * @param v1 some vertex
	 * @param v2 some vertex
	 */
	private int getMaxDominator(int v1, int v2) {
	    while (v1 != v2) {
		if (v1 == UNDEF || v2 == UNDEF)
		    throw new RuntimeException("no common dominator");
		if (v1 > v2)
		    v1 = preDominator[v1];
		else
		    v2 = preDominator[v2];
	    }
	    return v1;
	}

	/**
	 * @return the pre-dominator vertex of v
	 */
	public int getPreDominator(int v) {
	    if (preDominator[v] == UNDEF)
		throw new RuntimeException("pre-dominator for vertex " + v + " undefined");
	    else
		return preDominator[v];
	}
    }

    /**
     * @return a reducible flow graph with no loops or parallel arcs
     * @param vertexNr the number of vertices the RFG should have
     * @param fcArcNr the number of additional forward/cross arcs the RFG should have (total - (vertexNr-1))
     * @param bArcNr the number of backward arcs the RFG should have
     * @param singleLeaf if true, forward/cross arcs will be set as to avoid more than one leaf in the dag
     */
    public DirectedGraph createRandomRFG(int vertexNr, int fcArcNr, int bArcNr, boolean singleLeaf) {
	// init
	Random rand = new Random();
	Object[] vertices = new Object[vertexNr];
	DirectedGraph g = new DirectedMultigraph();
	PreDominatorTree preDom = new PreDominatorTree(vertexNr);
	// add vertices to graph
	for (int i = 0; i < vertexNr; i++) {
	    vertices[i] = new Integer(i);
	    g.addVertex(vertices[i]);
	}
	int source, target;
	// create forward arcs to establish flow
	for (int i = 1; i < vertexNr; i++) {
	    source = rand.nextInt(i);
	    g.addEdge(vertices[source], vertices[i]);
	    preDom.update(source, i);
	}
	// if singleLeaf then add cross/forward arcs to avoid more than 1 leaf is possible
	if (singleLeaf) {
	    source = 1;
	    while (source < vertexNr-1 /*&& fcArcNr > 0*/) {
		if (g.outDegreeOf(vertices[source]) == 0) {
		    target = (source + 1) + rand.nextInt(vertexNr - (source + 1));
		    g.addEdge(vertices[source], vertices[target]);
		    preDom.update(source, target);
		    fcArcNr--;
		}
		source++;
	    }
	    if (fcArcNr < 0) {
		//System.out.print("+" + (-fcArcNr) + "fcArcs");
		bArcNr += fcArcNr;
		fcArcNr = 0;
	    }
	}
	// add additional forward/cross arcs
	for (int i = 0; i < fcArcNr; ) {
	    source = rand.nextInt(vertexNr-1);
	    target = (source + 1) + rand.nextInt(vertexNr - (source + 1));
	    // no parallel arcs
	    if (!g.containsEdge(vertices[source], vertices[target])) {
		g.addEdge(vertices[source], vertices[target]);
		preDom.update(source, target);
		i++;
	    }
	}
	// add backward arcs
	for (int i = 0; i < bArcNr; ) {
	    source = rand.nextInt(vertexNr - 1) + 1;
	    // find dominating vertex as target
	    //target = preDom.getPreDominator(source);
	    target = source;
	    while (target > 0 && rand.nextBoolean())
		target = preDom.getPreDominator(target);
	    // no parallel arcs
	    if (!g.containsEdge(vertices[source], vertices[target])) {
		g.addEdge(vertices[source], vertices[target]);
		i++;
	    }
	}
	// done
	return g;
    }
}
