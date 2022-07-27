import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;
import contract.*;
import randomgraph.*;


/**
 * main class for testing the FS-contraction algorithm on random RFGs
 */
public class TestFS {

    private static class TestResult {
	private String name;
	private int
	    startVertexNr = 0,
	    finalVertexNr = 0,
	    startArcNr = 0,
	    finalArcNr = 0,
	    finalFONr = 0,
	    testNr = 0;
	private long
	    time = 0;

	public TestResult() {
	    this("Test results");
	}

	public TestResult(String name) {
	    this.name = name;
	}

	public void before(ContractGraph g) {
	    System.out.println(name + "-----------------------------------------");
	    System.out.println(g);
	    testNr++;
	    startVertexNr += g.vertexSet().size();
	    startArcNr += g.edgeSet().size();
	    time -= System.currentTimeMillis();
	}

	public void after(ContractGraph g) {
	    finalVertexNr += g.vertexSet().size();
	    finalArcNr += g.edgeSet().size();
	    time += System.currentTimeMillis();
	    finalFONr += g.getFONr();
	}

	public void report() {
	    System.out.println(name + ": vertices " + startVertexNr / testNr + " -> " + finalVertexNr / testNr +
			       ", arcs " + startArcNr / testNr + " -> " + finalArcNr / testNr +
			       ", FOs = " + finalFONr / testNr +
			       ", time = " + time/testNr + " ms");
	}
    }
	    
    /**
     * Executes alg on g, storing results in result
     */
    public static void run(FCRAlgorithm alg, DirectedGraph g, TestResult result) {
	ContractGraph cg = new ContractGraph();
	cg.addAllVertices(g.vertexSet());
	cg.addAllEdges(g.edgeSet());
	cg.weightArcs(false);
	result.before(cg);
	alg.run(cg);
	result.after(cg);
    }

    public static void runTransformed(FCRAlgorithm alg, DirectedGraph g, TestResult result) {
	ContractGraph cg = new ContractGraph();
	cg.addAllVertices(g.vertexSet());
	cg.addAllEdges(g.edgeSet());
	cg.weightArcs(false);
	cg = new FS2FVS().transform(cg);
	result.before(cg);
	alg.run(cg);
	result.after(cg);
    }

    private static DirectedGraph createGraph(int vertexNr, int[][] edges) {
	DirectedGraph g = new DirectedMultigraph();
	Object[] vertices = new Object[vertexNr];
	// create vertices
	for (int i = 0; i < vertexNr; i++) {
	    vertices[i] = new Integer(i);
	    g.addVertex(vertices[i]);
	}
	// create edges
	for (int i = 0; i < edges.length; i++)
	    g.addEdge(vertices[edges[i][0]], vertices[edges[i][1]]);
	return g;
    }

    public static void main(String[] args) {

	TestResult fsResult = new TestResult("FS-contraction");
	TestResult wfsResult = new TestResult("WFS-contraction");
	TestResult llResult = new TestResult("LL-contraction");

	if (true) {
	    // parameters
	    int testNr = 1,
		vertexNr = 6,
		bArcNr = vertexNr / 2,
		fcArcNr = (vertexNr + 1) - bArcNr;

	    for (int i = 0; i < testNr; i++) {
		// create random RFG
		DirectedGraph rfg = new RandomRFG().createRandomRFG(vertexNr, fcArcNr, bArcNr, true);
		run(new FSAlgorithm(), rfg, fsResult);
		run(new WFSAlgorithm(), rfg, wfsResult);
		runTransformed(new LLAlgorithm(), rfg, llResult);
		System.out.print(".");
	    }
	    System.out.println("");
	} else {
	    // special tests
	    DirectedGraph g = createGraph(6, new int[][] {{0,1},{1,2},{1,3},{2,3},{3,1},{3,4},{4,0},{4,5},{5,5}});
	    run(new FSAlgorithm(), g, fsResult);
	    run(new WFSAlgorithm(), g, wfsResult);
	    runTransformed(new LLAlgorithm(), g, llResult);
	}

	// report results
	fsResult.report();
	wfsResult.report();
	llResult.report();
    }

}
