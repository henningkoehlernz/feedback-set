package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import fcr.*;
import util.*;


/**
 * provides methods for updating observers whenever the graph gets modified
 */
public class UpdateGraph extends DirectedMultigraph implements FCRObject {

    /**
     * constants for identifying events (for updates)
     */
    public static final Object ARC_ADDED = "arc_added";
    public static final Object ARC_REMOVED = "arc_removed";
    public static final Object ARC_CHANGED = "arc_changed";
    public static final Object VERTEX_ADDED = "vertex_added";
    public static final Object VERTEX_REMOVED = "vertex_removed";
    public static final Object VERTEX_CHANGED = "vertex_changed";

    /**
     * stores the data associated with an update, which is passed along by notifyObservers
     */
    public class UpdateData {

	public Object obj, event;
	
	public UpdateData(Object obj, Object event) {
	    this.obj = obj;
	    this.event = event;
	}
    }

    protected Observable observable = new MyObservable();

    public Observable getObservable() {
	return observable;
    }

    protected void notifyObservers(Object obj, Object event) {
	observable.notifyObservers(new UpdateData(obj, event));
	//System.out.println("UdpateGraph: notifying " + observable);
    }

    /**
     * fix: did not call addEdge(edge)
     */
    public Edge addEdge(Object source, Object target) {
	Edge edge = super.addEdge(source, target);
	if (edge != null) {
	    notifyObservers(edge, ARC_ADDED);
	}
	return edge;
    }

    public boolean addEdge(Edge edge) {
	if (super.addEdge(edge)) {
	    notifyObservers(edge, ARC_ADDED);
	    return true;
	}
	else
	    return false;
    }

    public boolean removeEdge(Edge edge) {
	if (super.removeEdge(edge)) {
	    notifyObservers(edge, ARC_REMOVED);
	    return true;
	}
	else
	    return false;
    }

    public boolean addVertex(Object vertex) {
	if (super.addVertex(vertex)) {
	    notifyObservers(vertex, VERTEX_ADDED);
	    return true;
	}
	else
	    return false;
    }

    public boolean removeVertex(Object vertex) {
	if (super.removeVertex(vertex)) {
	    notifyObservers(vertex, VERTEX_REMOVED);
	    return true;
	}
	else
	    return false;
    }
   

}
