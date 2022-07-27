import java.util.*;

import feedback.*;
import grasp.*;

public class ReductionTest {

    private static RandomGraphGenerator gen = new RandomGraphGenerator();
    private static Random rand = new Random();

    private static int simpleSteps = 0;
    private static int fullSteps = 0;
    private static double simpleWeight = 0;
    private static double fullWeight = 0;

    public static void main(String[] args) {
	// specific test
	/*
	System.out.println("parallel test:");
	test(createGraph(new double[] {2,1,1,1,1},
			 new int[][] {{0,1}, {0,2}, {1,3}, {1,4}, {2,3}, {2,4}, {3,0}, {4,0}}));
	test(createGraph(new double[] {1,1,1},
			 new int[][] {{0,1}, {1,2}, {2,0}}));
	*/
	// random tests..
	for (int i = 0; i < 100; i++) {
	    //test(1000, 2000);
	    testGRASP(100, 300);
	    System.out.print(".");
	}
	//System.out.println("\nSimple steps: " + simpleSteps + ", Full steps: " + fullSteps);
	System.out.println("\nSimple weight: " + simpleWeight + ", Full weight: " + fullWeight);
	
    }

    private static void test(int vertexNr, int edgeNr) {
	test(gen.generateFeedbackVertexGraph(vertexNr, edgeNr));
    }

    private static void testGRASP(int vertexNr, int edgeNr) {
	testGRASP(gen.generateFeedbackVertexGraph(vertexNr, edgeNr));
    }

    public static void testGRASP(FeedbackVertexGraph g) {
	g.init();
	simpleWeight +=
	    testProblem(new FVSProblem(g, new FeedbackVertexReducer(FeedbackVertexReducer.reduceSimple)));
	fullWeight +=
	    testProblem(new FVSProblem(g, new FeedbackVertexReducer(FeedbackVertexReducer.reduceFull)));
    }

    public static double testProblem(FVSProblem problem) {
	// reduce once for better performance
	problem.reduce();
	Object solution = new GRASPSolver().solve(problem, 10);
	return problem.getSolutionWeight(solution);
    }

    private static void test(FeedbackVertexGraph g) {
	// reduce once simple to speed things up..
	g.initReductionVertices();
	new FeedbackVertexReducer().reduceSimple(g, new HashSet());
	// now operate on clones only..
	FeedbackVertexGraph clone = (FeedbackVertexGraph)g.clone();
	clone.init();
	//System.out.println("simple clone =" + clone);
	// reduce to empty by first iterating between reduction and random vertex removal
	while (!clone.vertexSet().isEmpty()) {
	    Object[] vertices = clone.vertexSet().toArray();
	    clone.removeVertex(vertices[rand.nextInt(vertices.length)]);
	    simpleSteps++;
	    new FeedbackVertexReducer().reduceSimple(clone, new HashSet());
	}
	// now same with full reduction power..
	clone = (FeedbackVertexGraph)g.clone();
	clone.init();
	//System.out.println("clone =" + clone);
	new FeedbackVertexReducer().reduceFull(clone, new HashSet());
	// reduce to empty by first iterating between reduction and random vertex removal
	while (!clone.vertexSet().isEmpty()) {
	    Object[] vertices = clone.vertexSet().toArray();
	    clone.removeVertex(vertices[rand.nextInt(vertices.length)]);
	    fullSteps++;
	    //new FeedbackVertexReducer().reduceFull(clone, new HashSet());
	    new FeedbackVertexReducer().reduceUpdate(clone, new HashSet());
	}
    }

    private static FeedbackVertexGraph createGraph(double[] weights, int[][] edges) {
	FeedbackVertexGraph g = new FeedbackVertexGraph();
	// create vertices
	Object[] vertices = new Object[weights.length];
	for (int i = 0; i < weights.length; i++) {
	    Set idSet = new HashSet();
	    idSet.add(new Integer(i));
	    vertices[i] = new FeedbackVertex(weights[i], idSet);
	    g.addVertex(vertices[i]);
	}
	// create edges
	for (int i = 0; i < edges.length; i++)
	    g.addEdge(vertices[edges[i][0]], vertices[edges[i][1]]);
	return g;
    }

}
