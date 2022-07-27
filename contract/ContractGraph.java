package contract;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

/**
 * graph class for contractions, keeps track of arc/vertex -> IDs & weight mapping and notifies of changes
 * also stores a (feedback) set of IDs
 */
public class ContractGraph extends UpdateGraph {

    public static final int INFINITY = -1;

    protected HashMap mapObjToIDs = new HashMap();
    protected HashSet storedIDs = new HashSet();

    /**
     * used to implement the arc/vertex -> IDs & weight mapping
     */
    protected class MapData {

	public Collection idSet;
	public int weight;

	public MapData(Collection idSet, int weight) {
	    this.idSet = idSet;
	    this.weight = weight;
	}

	public MapData(Object id, int weight) {
	    idSet = new ArrayList(1);
	    idSet.add(id);
	    this.weight = weight;
	}
    }
    
    /**
     * adds a vertex with given weight to the graph;
     * the id associated with the vertex will be the vertex itself
     */
    public boolean addVertex(Object vertex, int weight) {
	if (super.addVertex(vertex)) {
	    if (weight != INFINITY)
		mapObjToIDs.put(vertex, new MapData(vertex, weight));
	    return true;
	}
	else
	    return false;
    }

    /**
     * adds an edge with given weight to the graph;
     * the id associated with the edge will be the edge itself
     */
    public boolean addEdge(Edge edge, int weight) {
	if (super.addEdge(edge)) {
	    mapObjToIDs.put(edge, new MapData(edge, weight));
	    return true;
	} else
	    return false;
    }

    /**
     * turns the graph into a FAS graph
     * @param weighted if true arc weights will be random in ]0,1], otherwise 1
     */
    public void weightArcs(boolean weighted) {
	Object[] arcs = edgeSet().toArray();
	if (weighted) {
	    Random rand = new Random();
	    for (int i = 0; i < arcs.length; i++)
		mapObjToIDs.put(arcs[i], new MapData(arcs[i], 1 + rand.nextInt(100)));
	} else {
	    for (int i = 0; i < arcs.length; i++)
		mapObjToIDs.put(arcs[i], new MapData(arcs[i], 1));
	}
    }

    /**
     * turns the graph into a FVS graph
     * @param weighted if true vertex weights will be random in ]0,1], otherwise 1
     */
    public void weightVertices(boolean weighted) {
	Object[] vertices = vertexSet().toArray();
	if (weighted) {
	    Random rand = new Random();
	    for (int i = 0; i < vertices.length; i++)
		mapObjToIDs.put(vertices[i], new MapData(vertices[i], 1 + rand.nextInt(100)));
	} else {
	    for (int i = 0; i < vertices.length; i++)
		mapObjToIDs.put(vertices[i], new MapData(vertices[i], 1));
	}
    }

    public boolean removeEdge(Edge edge) {
	mapObjToIDs.remove(edge);
	return super.removeEdge(edge);
    }

    public boolean removeVertex(Object vertex) {
	mapObjToIDs.remove(vertex);
	return super.removeVertex(vertex);
    }
    
    /*
    public MapData getData(Object obj) {
	return (MapData)mapObjToIDs.get(obj);
    }
	
    public Collection getIDs(Object obj) {
	MapData data = (MapData)mapObjToIDs.get(obj);
	if (data == null)
	    return null;
	else
	    return data.idSet;
    }
    */

    /**
     * @return nr of feedback objects, that is nr of arcs/vertices with finite weight
     */
    public int getFONr() {
	return mapObjToIDs.size();
    }

    /**
     * @return the weight of obj
     */
    public int getWeight(Object obj) {
	MapData data = (MapData)mapObjToIDs.get(obj);
	if (data == null)
	    return INFINITY;
	else
	    return data.weight;
    }

    /**
     * notifies observers that a change in obj has occured
     */
    protected void notifyChanged(Object obj) {
	if (obj instanceof Edge && containsEdge((Edge)obj))
	    notifyObservers(obj, ARC_CHANGED);
	else
	    notifyObservers(obj, VERTEX_CHANGED);
    }

    /**
     * substitutes the data (IDs and weight) of obj1 with those of obj2; obj2 won't have any data associated with it
     */
    public void substData(Object obj1, Object obj2) {
	MapData data2 = (MapData)mapObjToIDs.get(obj2);
	if (data2 == null) {
	    mapObjToIDs.remove(obj1);
	    notifyChanged(obj1);
	} else {
	    // update arc/vertex -> IDs & weight
	    mapObjToIDs.put(obj1, data2);
	    notifyChanged(obj1);
	    mapObjToIDs.remove(obj2);
	    notifyChanged(obj2);
	}
    }

    /**
     * joins the data (IDs and weight) of obj1 with those of obj2; obj2 won't have any data associated with it
     */
    public void joinData(Object obj1, Object obj2) {
	MapData data1 = (MapData)mapObjToIDs.get(obj1);
	MapData data2 = (MapData)mapObjToIDs.get(obj2);
	// if one obj has no data associated with it
	if (data1 == null) {
	    if (data2 != null) {
		mapObjToIDs.remove(obj2);
		notifyChanged(obj2);
	    }
	} else {
	    if (data2 == null) {
		mapObjToIDs.remove(obj1);
		notifyChanged(obj1);
	    } else {
		mapObjToIDs.remove(obj2);
		notifyChanged(obj2);
		// update arc/vertex -> IDs & weight
		data1.idSet.addAll(data2.idSet);
		if (data1.weight == INFINITY || data2.weight == INFINITY)
		    data1.weight = INFINITY;
		else
		    data1.weight += data2.weight;
		notifyChanged(obj1);
	    }
	}
    }

    /**
     * adds the IDs associated with obj to the set of stored IDs
     * throws IllegalArgumentException if obj has no data associated with it
     * @param obj the arc or vertex to be stored
     */
    public void store(Object obj) throws IllegalArgumentException {
	MapData data = (MapData)mapObjToIDs.get(obj);
	if (data == null)
	    throw new IllegalArgumentException("Object [" + obj + "] has no associated data!");
	storedIDs.addAll(data.idSet);
    }

    /**
     * @return the set of IDs stored via "store"
     */
    public Collection getStoredIDs() {
	return (Collection)storedIDs.clone();
    }

    public String toString() {
	String out = super.toString() + " : { ";
	Iterator it = mapObjToIDs.keySet().iterator();
	while (it.hasNext()) {
	    Object key = it.next();
	    out += key + "->" + getWeight(key) + " ";
	}
	out += "}";
	return out;
    }

}
