package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

/*
 * transforms a FS graph into a FVS graph
 */
public class FS2FVS implements ContractGraphTransformer {

    public ContractGraph transform(ContractGraph g) {
	ContractGraph fvsGraph = new ContractGraph();
	Iterator it = g.vertexSet().iterator();
	// copy vertices
	while (it.hasNext()) {
	    Object vertex = it.next();
	    int weight = g.getWeight(vertex);
	    fvsGraph.addVertex(vertex, weight);
	}
	// copy arcs, replaceing weighted arcs by vertices
	it = g.edgeSet().iterator();
	int vertexCount = g.vertexSet().size();
	while (it.hasNext()) {
	    Edge arc = (Edge)it.next();
	    int weight = g.getWeight(arc);
	    if (weight == ContractGraph.INFINITY)
		fvsGraph.addEdge(arc);
	    else {
		Object vertex = new Integer(vertexCount++);
		fvsGraph.addVertex(vertex, weight);
		fvsGraph.addEdge(arc.getSource(), vertex);
		fvsGraph.addEdge(vertex, arc.getTarget());
	    }
	}
	return fvsGraph;
    }

}
