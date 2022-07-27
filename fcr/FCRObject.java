package fcr;

import java.util.*;

/**
 * A FCRObject represents an element of the domain of a FCR relation. It contains the data which gets transformed by the FCRAlgorithm.
 * Whenever the FCRObject gets modified (transformed), it must notify its Observers which (directly or indirectly) are the FCRSteps.
 */
public interface FCRObject {

    /**
     * @return the Observable object handling the notification process
     */
    public Observable getObservable();

}
