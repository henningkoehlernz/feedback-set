package fcr;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;
import org._3pq.jgrapht.edge.*;

import util.*;

/**
 * provides a simple implementation for the FCRAlgorithm interface
 * in this, updates are sent from the FCRObject to the FCRAlgorithm which forwards them to registered steps
 */
public class CFCRAlgorithm implements FCRAlgorithm {

    /**
     * handles steps of the same priority level
     */
    protected class PriorityLevel extends Vector {

	private final int NO_CHANGE = -1;
	private int lastChanged = NO_CHANGE, current = 0;
	
	/**
	 * @return the currently active step
	 */
	public FCRStep getCurrentStep() {
	    return (FCRStep)get(current);
	}

	/**
	 * sets the last changed step to the current one
	 */
	public void setChanged() {
	    lastChanged = current;
	}

	/**
	 * @return true if the last change ocurred in the current step or no steps available
	 */
	public boolean isDone() {
	    return size() == 0 || lastChanged == current;
	}

	/**
	 * advances the current step to be the 'next' step of this priority level
	 */
	public void advance() {
	    if (++current >= size()) {
		if (lastChanged == NO_CHANGE)
		    lastChanged = 0;
		current = 0;
	    }
	}

	/**
	 * resets current and lastChanged step
	 */
	public void reset() {
	    lastChanged = NO_CHANGE;
	    current = 0;
	}

    }

    /**
     * handles contraction steps by priority level
     */
    protected class PriorityTable {

	private Vector levels = new Vector();

	/**
	 * adds a step of given priority
	 * @param priority must not be negative
	 */
	public void addStep(FCRStep step, int priority) {
	    if (priority < 0)
		throw new IllegalArgumentException("negative priority: " + priority);
	    if (levels.size() < priority + 1)
		levels.setSize(priority + 1);
	    PriorityLevel level = (PriorityLevel)levels.get(priority);
	    if (level == null) {
		level = new PriorityLevel();
		levels.set(priority, level);
	    }
	    level.add(step);
	}

	/**
	 * @return the PriorityLevel object for priority
	 */
	public PriorityLevel getLevel(int priority) {
	    return (PriorityLevel)levels.get(priority);
	}

	/**
	 * @return the smallest priority level such that no steps have been added of this level or higher
	 */
	public int maxLevel() {
	    return levels.size();
	}

	/**
	 * iterator class returned by iterator()
	 */
	protected class PriorityTableIterator implements Iterator {

	    private int level = 0; // current level
	    private int index = 0; // current index on current level
	    
	    public PriorityTableIterator() {
		skipBlank();
	    }

	    private void skipBlank() {
		PriorityLevel pLevel;
		while (level < maxLevel() && ((pLevel = getLevel(level)) == null || index == pLevel.size())) {
		    level++;
		    index = 0;
		}
	    }

	    public boolean hasNext() {
		return level < maxLevel();
	    }

	    public Object next() {
		if (level >= maxLevel())
		    throw new NoSuchElementException();
		Object elem = getLevel(level).get(index);
		index++;
		skipBlank();
		return elem;
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	    
	}

	/**
	 * @return an iterator that iterates through all the contraction steps on different levels
	 */
	public Iterator iterator() {
	    return new PriorityTableIterator();
	}
    }

    /**
     * registered FCRSteps, organized by PriorityLevels
     */
    protected PriorityTable steps = new PriorityTable();

    /**
     * current FCRObject
     */
    protected FCRObject fcrObj = null;

    /**
     * Observable object handling the notification process
     */
    protected Observable observable = new MyObservable();

    protected Vector updates = new Vector();

    /**
     * registers a FCRStep as part of the algorithm
     * @param step the FCRStep to be registered
     */
    public void register(FCRStep step) {
	register(step, 0);
    }

    /**
     * registers a FCRStep as part of the algorithm
     * @param step the FCRStep to be registered
     * @param priority the priority to be assigned to the step; \
       steps are only applied when no lower-priority steps can be applied; \
       must not be negative
     */
    public void register(FCRStep step, int priority) {
	steps.addStep(step, priority);
	observable.addObserver(step);
	step.setAlgorithm(this);
    }

    /**
     * to be called by the individual FCRSteps
     * @return the FCRObject which the algorithm is currently being applied to
     */
    public FCRObject getObject() {
	return fcrObj;
    }

    public void update(Observable o, Object arg) {
	updates.add(arg);
    }

    /**
     * forwards all updates stored to observers, then clears update list
     */
    protected void forwardUpdate() {
	//System.out.println("forwarding updates to " + observable);
	for (int i = 0; i < updates.size(); i++) {
	    observable.notifyObservers(updates.get(i));
	}
	updates.clear();
    }

    /**
     * initializes all registered steps by calling initApply()
     */
    protected void initSteps() {
	Iterator it = steps.iterator();
	while (it.hasNext())
	    ((FCRStep)it.next()).initApply();
    }

    /**
     * runs the algorithm on the given FCRObject by applying registered FCRSteps until no more steps can be applied
     */
    public void run(FCRObject fcrObj) {
	this.fcrObj = fcrObj;
	int currentLevel = 0;
	FCRStep step;
	PriorityLevel level;
	initSteps();
	fcrObj.getObservable().addObserver(this);
	while (currentLevel < steps.maxLevel()) {
	    //System.out.print(".");
	    // get current step
	    level = steps.getLevel(currentLevel);
	    if (level == null) {
		currentLevel++;
		continue;
	    }
	    step = level.getCurrentStep();
	    //System.out.println("Step: " + step);
	    // try to apply it
	    if (step.apply()) {
		level.setChanged();
		// back to level 0
		currentLevel = 0;
		// make sure that lower levels are reset..
		forwardUpdate();
	    } else {
		level.advance();
		if (level.isDone()) {
		    level.reset();
		    currentLevel++;
		}
	    }
	}
	fcrObj.getObservable().deleteObserver(this);
    }

}
