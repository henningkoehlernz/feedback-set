package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

public class RandomGraphGenerator {

    private Random rand = null;

    public RandomGraphGenerator() {
	rand = new Random();
    }

    public RandomGraphGenerator(int seed) {
	rand = new Random(seed);
    }

    /**
     * generates a random FeedbackVertexGraph
     * @param weighted if true vertices will have random weight in ]0;1], else all weights are 1
     * @param local the maximal index difference between 2 vertices
     */
    public FeedbackVertexGraph generateFeedbackVertexGraph(int vertexNr, int edgeNr,
							   boolean weighted, int local) {
	if (edgeNr > (vertexNr - 1) * vertexNr / 2)
	    throw new IllegalArgumentException("too many edges: " + edgeNr);
	// init graph
	FeedbackVertexGraph g = new FeedbackVertexGraph();
	// create vertices
	FeedbackVertex[] vertices = new FeedbackVertex[vertexNr];
	for (int i = 0; i < vertexNr; i++) {
	    Set idSet = new HashSet();
	    idSet.add(new Integer(i));
	    if (weighted)
		vertices[i] = new FeedbackVertex(1.0 - rand.nextDouble(), idSet);
	    else
		vertices[i] = new FeedbackVertex(1.0, idSet);
	    g.addVertex(vertices[i]);
	}
	// create edges
	int edgesAdded = 0;
	while (edgesAdded < edgeNr) {
	    int source = rand.nextInt(vertexNr);
	    int target = source + rand.nextInt(2 * local + 1) - local;
	    // modulo..
	    target = target % vertexNr;
	    if (target < 0)
		target += vertexNr;
	    // loop or already existing?
	    if (source == target || g.containsEdge(vertices[source], vertices[target]))
		continue;
	    g.addEdge(vertices[source], vertices[target]);
	    edgesAdded++;
	}

	// assign ugly weights..
	/*
	for (int i = 0; i < vertexNr; i++)
	    vertices[i].setWeight(g.inDegreeOf(vertices[i]) + g.outDegreeOf(vertices[i]));
	*/
	return g;
    }

    public FeedbackVertexGraph generateFeedbackVertexGraph(int vertexNr, int edgeNr) {
	return generateFeedbackVertexGraph(vertexNr, edgeNr, false, Math.max(3, edgeNr/vertexNr));
	//return generateFeedbackVertexGraph(vertexNr, edgeNr, true, vertexNr);
    }
}
