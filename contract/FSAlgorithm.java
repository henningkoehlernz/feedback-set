package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;


public class FSAlgorithm extends CFCRAlgorithm {

    public FSAlgorithm() {
	this(true);
    }

    public FSAlgorithm(boolean register) {
	if (register)
	    registerSteps();
    }

    protected void registerSteps() {
	register( new StepLoop()         , 1 );
	register( new StepLoopArc()      , 1 );
	register( new StepInOut0()       , 1 );
	register( new StepIn1Fin()       , 1 );
	register( new StepOut1Fin()      , 1 );
	register( new StepIn1Inf()       , 1 );
	register( new StepOut1Inf()      , 1 );
	register( new StepInfinityCycle(), 1 );
	register( new StepInfinityMark() , 1 );
	register( new StepIO1()          , 1 );
	register( new StepParallel()     , 0 );
    }

    private static final int INFINITY = ContractGraph.INFINITY;

    // loop
    public class StepLoop extends ArcContractionStep {

	protected void apply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getSource();
	    g.store(vertex);
	    g.removeVertex(vertex);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    return edge.getSource() == edge.getTarget()
		&& g.getWeight(edge) == INFINITY;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_ADDED || event == UpdateGraph.ARC_CHANGED)
		store(obj);
	}
    }

    // loop-arc
    public class StepLoopArc extends ArcContractionStep {

	protected void apply(Edge edge, ContractGraph g) {
	    g.store(edge);
	    g.removeEdge(edge);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    return edge.getSource() == edge.getTarget()
		&& g.getWeight(edge.getSource()) == INFINITY;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_ADDED)
		store(obj);
	}
    }

    // in0 & out0
    public class StepInOut0 extends VertexContractionStep {

	protected void apply(Object obj, ContractGraph g) {
	    g.removeVertex(obj);
	}
	
	protected boolean canApply(Object obj, ContractGraph g) {
	    return g.inDegreeOf(obj) == 0 || g.outDegreeOf(obj) == 0;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_REMOVED) {
		Edge edge = (Edge)obj;
		store(edge.getSource());
		store(edge.getTarget());
	    }
	}
    }
    
    /**
     * base class for in1-fin and in1-inf
     */
    public abstract class StepIn1 extends ArcContractionStep {

	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_REMOVED) {
		Edge edge = (Edge)obj;
		// store incoming edge..
		Object vertex = edge.getTarget();
		DirectedGraph g = (DirectedGraph)alg.getObject();
		if (g.containsVertex(vertex) && g.inDegreeOf(vertex) == 1)
		    store(g.incomingEdgesOf(vertex).get(0));
	    }
	}

     }

    /**
     * base class for out1-fin and out1-inf
     */
    public abstract class StepOut1 extends ArcContractionStep {

	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_REMOVED) {
		Edge edge = (Edge)obj;
		// store outgoing edge..
		Object vertex = edge.getSource();
		DirectedGraph g = (DirectedGraph)alg.getObject();
		if (g.containsVertex(vertex) && g.outDegreeOf(vertex) == 1)
		    store(g.outgoingEdgesOf(vertex).get(0));
	    }
	}
     }

    // in1-fin
    public class StepIn1Fin extends StepIn1 {

	protected void apply(Edge edge, ContractGraph g) {
	    g.substData(edge.getTarget(), edge);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getTarget();
	    int edgeWeight = g.getWeight(edge);
	    int vertexWeight = g.getWeight(vertex);
	    return g.inDegreeOf(vertex) == 1
		&& edgeWeight != INFINITY && (vertexWeight == INFINITY || edgeWeight <= vertexWeight);
	}
    }
    
    // out1-fin
    public class StepOut1Fin extends StepOut1 {

	protected void apply(Edge edge, ContractGraph g) {
	    g.substData(edge.getSource(), edge);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getSource();
	    int edgeWeight = g.getWeight(edge);
	    int vertexWeight = g.getWeight(vertex);
	    return g.outDegreeOf(vertex) == 1
		&& edgeWeight != INFINITY && (vertexWeight == INFINITY || edgeWeight <= vertexWeight);
	}
    }

    // in1-inf
    public class StepIn1Inf extends StepIn1 {

	protected void apply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getTarget();
	    Object pre = edge.getSource();
	    Iterator it = g.outgoingEdgesOf(vertex).iterator();
	    // move outgoing arcs
	    while (it.hasNext()) {
		Edge nextEdge = (Edge)it.next();
		Edge newEdge = g.addEdge(pre, nextEdge.getTarget());
		g.substData(newEdge, nextEdge);
	    }
	    g.removeVertex(vertex);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getTarget();
	    Object pre = edge.getSource();
	    int edgeWeight = g.getWeight(edge);
	    int vertexWeight = g.getWeight(vertex);
	    int preWeight = g.getWeight(pre);
	    return g.inDegreeOf(vertex) == 1
		&& pre != vertex
		&& g.getWeight(edge) == INFINITY
		&& (vertexWeight == INFINITY || (preWeight != INFINITY && preWeight <= vertexWeight));
	}
    }
    
    // out1-inf
    public class StepOut1Inf extends StepOut1 {

	protected void apply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getSource();
	    Object succ = edge.getTarget();
	    Iterator it = g.incomingEdgesOf(vertex).iterator();
	    // move outgoing arcs
	    while (it.hasNext()) {
		Edge nextEdge = (Edge)it.next();
		Edge newEdge = g.addEdge(nextEdge.getSource(), succ);
		g.substData(newEdge, nextEdge);
	    }
	    g.removeVertex(vertex);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Object vertex = edge.getSource();
	    Object succ = edge.getTarget();
	    int edgeWeight = g.getWeight(edge);
	    int vertexWeight = g.getWeight(vertex);
	    int succWeight = g.getWeight(succ);
	    return g.outDegreeOf(vertex) == 1
		&& succ != vertex
		&& g.getWeight(edge) == INFINITY
		&& (vertexWeight == INFINITY || (succWeight != INFINITY && succWeight <= vertexWeight));
	}
    }

    // infinity-cycle, both v->v_inf and v_inf->v
    // less trouble with parallel arcs that way - only updateEvent might still contain parallel arcs
    public class StepInfinityCycle extends ArcContractionStep {
	
	// may assume that no parallel arcs exist
	protected void apply(Edge edge, ContractGraph g) {
	    Object vertex;
	    if (g.getWeight(edge.getSource()) != INFINITY)
		vertex = edge.getSource();
	    else
		vertex = edge.getTarget();
	    g.store(vertex);
	    g.removeVertex(vertex);
	}
	
	// may assume that no parallel arcs exist
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Object source = edge.getSource(),
		target = edge.getTarget();
	    Edge antiEdge = g.getEdge(target, source);
	    return antiEdge != null
		&& g.getWeight(edge) == INFINITY
		&& g.getWeight(antiEdge) == INFINITY
		&& (g.getWeight(source) == INFINITY || g.getWeight(target) == INFINITY);
	}
	
	// may NOT assume that no parallel arcs exist !!!
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_ADDED || event == UpdateGraph.ARC_CHANGED)
		store(obj);
	}
    }   

    // infinity-mark
    public class StepInfinityMark extends ArcContractionStep {
	
	protected void apply(Edge edge, ContractGraph g) {
	    g.substData(edge, null);
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    int edgeWeight = g.getWeight(edge);
	    int sourceWeight = g.getWeight(edge.getSource());
	    int targetWeight = g.getWeight(edge.getTarget());
	    return edgeWeight != INFINITY
		&& ((sourceWeight != INFINITY && sourceWeight <= edgeWeight)
		    || (targetWeight != INFINITY && targetWeight <= edgeWeight));
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_ADDED || event == UpdateGraph.ARC_CHANGED)
		store(obj);
	    // this is time-critical..
	    if (event == UpdateGraph.VERTEX_CHANGED) {
		ContractGraph g = (ContractGraph)getObject();
		if (g.containsVertex(obj)) {
		    Iterator it = g.edgesOf(obj).iterator();
		    while (it.hasNext())
			store(it.next());
		}
	    }
	}
    }
    
    // io1
    public class StepIO1 extends VertexContractionStep {
	
	protected void apply(Object vertex, ContractGraph g) {
	    Object pre = ((Edge)g.incomingEdgesOf(vertex).iterator().next()).getSource();
	    Object succ = ((Edge)g.outgoingEdgesOf(vertex).iterator().next()).getTarget();
	    Edge edge = g.addEdge(pre, succ);
	    g.substData(edge, vertex);
	    g.removeVertex(vertex);
	}
	
	protected boolean canApply(Object vertex, ContractGraph g) {
	    if (!(g.inDegreeOf(vertex) == 1 && g.outDegreeOf(vertex) == 1))
		return false;
	    Edge inEdge = (Edge)g.incomingEdgesOf(vertex).iterator().next();
	    Edge outEdge = (Edge)g.outgoingEdgesOf(vertex).iterator().next();
	    return inEdge.getSource() != vertex
		&& g.getWeight(inEdge) == INFINITY
		&& g.getWeight(outEdge) == INFINITY;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_REMOVED || event == UpdateGraph.ARC_CHANGED) {
		Edge edge = (Edge)obj;
		store(edge.getSource());
		store(edge.getTarget());
	    }
	}
    }
    
    // parallel - merges all parallel arcs at once
    public class StepParallel extends ArcContractionStep {
	
	protected void apply(Edge edge, ContractGraph g) {
	    Edge[] edges = (Edge[])g.getAllEdges(edge.getSource(), edge.getTarget()).toArray(new Edge[0]);
	    for (int i = 0; i < edges.length; i++)
		if (edges[i] != edge) {
		    g.joinData(edge, edges[i]);
		    g.removeEdge(edges[i]);
		}
	    //System.out.print("p");
	}
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    int edgeNr = g.getAllEdges(edge.getSource(), edge.getTarget()).size();
	    return edgeNr > 1;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.ARC_ADDED)
		store(obj);
	}
    }   

}
