package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;


public class LLAlgorithm extends FSAlgorithm {

    public LLAlgorithm() {
	super(false);
	register( new StepInfinityReduce()           , 1 );
	register( new FSAlgorithm.StepLoop()         , 1 );
	register( new FSAlgorithm.StepInOut0()       , 1 );
	register( new FSAlgorithm.StepIn1Inf()       , 1 );
	register( new FSAlgorithm.StepOut1Inf()      , 1 );
    }

    private static final int INFINITY = ContractGraph.INFINITY;

    // infinity-reduce
    public class StepInfinityReduce extends VertexContractionStep {
	
	protected void apply(Object vertex, ContractGraph g) {
	    // get predecessors
	    Object[] pre = g.incomingEdgesOf(vertex).toArray();
	    for (int i = 0; i < pre.length; i++)
		pre[i] = ((Edge)pre[i]).getSource();
	    // get successors
	    Object[] succ = g.outgoingEdgesOf(vertex).toArray();
	    for (int i = 0; i < succ.length; i++)
		succ[i] = ((Edge)succ[i]).getTarget();
	    // add direct connections
	    for (int iPre = 0; iPre < pre.length; iPre++)
		for (int iSucc = 0; iSucc < succ.length; iSucc++)
		    g.addEdge(pre[iPre], succ[iSucc]);
	    g.removeVertex(vertex);
	}
	
	protected boolean canApply(Object vertex, ContractGraph g) {
	    return g.getWeight(vertex) == INFINITY;
	}
	
	protected void updateEvent(Object obj, Object event) {
	    if (event == UpdateGraph.VERTEX_ADDED)
		store(obj);
	}
    }

}
