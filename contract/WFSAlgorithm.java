package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;


public class WFSAlgorithm extends FSAlgorithm {

    public WFSAlgorithm() {
	super(false);
	register( new FSAlgorithm.StepLoop()         , 1 );
	register( new FSAlgorithm.StepLoopArc()      , 1 );
	register( new FSAlgorithm.StepInOut0()       , 1 );
	register( new FSAlgorithm.StepIn1Fin()       , 1 );
	register( new FSAlgorithm.StepOut1Fin()      , 1 );
	register( new FSAlgorithm.StepIn1Inf()       , 1 );
	register( new FSAlgorithm.StepOut1Inf()      , 1 );
	register( new FSAlgorithm.StepInfinityCycle(), 1 );
	register( new FSAlgorithm.StepInfinityMark() , 1 );
	register( new FSAlgorithm.StepIO1()          , 1 );
	register( new StepInfinityParallel()         , 0 );
    }

    private static final int INFINITY = ContractGraph.INFINITY;

    // infinity-parallel - merges all parallel arcs at once
    public class StepInfinityParallel extends FSAlgorithm.StepParallel {
	
	protected boolean canApply(Edge edge, ContractGraph g) {
	    Collection edgeSet = g.getAllEdges(edge.getSource(), edge.getTarget());
	    if (edgeSet.size() < 2)
		return false;
	    Iterator it = edgeSet.iterator();
	    while (it.hasNext())
		if (g.getWeight(it.next()) == INFINITY)
		    return true;
	    return false;
	}
	
    }   

}
