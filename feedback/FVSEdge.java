package feedback;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

public class FVSEdge extends DirectedEdge {

    // number of missing predecessor -> target connections
    private int preConnect = 0;
    // number of missing source -> successor connections
    private int succConnect = 0;

    public FVSEdge(Object source, Object target) {
	super(source, target);
    }

    public int getPreConnect() {
	return preConnect;
    }

    public int getSuccConnect() {
	return succConnect;
    }

    public void setPreConnect(int newValue, Set zeroAdd) {
	if (newValue < 0)
	    throw new IllegalArgumentException(this + ": newValue = " + newValue);
	preConnect = newValue;
	if (preConnect == 0)
	    zeroAdd.add(this);
    }

    public void setSuccConnect(int newValue, Set zeroAdd) {
	if (newValue < 0)
	    throw new IllegalArgumentException(this + ": newValue = " + newValue);
	succConnect = newValue;
 	if (succConnect == 0)
	    zeroAdd.add(this);
   }

    public void incPreConnect() {
	setPreConnect(preConnect + 1, null);
    }

    public void incSuccConnect() {
	setSuccConnect(succConnect + 1, null);
    }

    public void decPreConnect(Set zeroAdd) {
	setPreConnect(preConnect - 1, zeroAdd);
    }

    public void decSuccConnect(Set zeroAdd) {
	setSuccConnect(succConnect - 1, zeroAdd);
    }

    public String toString() {
	return super.toString() + ":" + preConnect + "/" + succConnect;
    }

}
