package SequenceMining.SupportStructures;

import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure that contains the "projected" version of one transaction
 */
public class Transaction {

    /**
     * Map that for each singular item (key) contains as a value all its possible positions in the transaction;
     * this list of positions is contained in a nested hash map that has incremental integers as keys and the actual
     * symbol's positions as values.
     * This kind of structure guarantees that all accesses have complexity O(1)
     */
    private Map < String, HashMap < Integer, Integer > > transactionMapping;

    /**
     * Support structure used during the initialization of the class, used to
     * know the last incremental index of every singular item
     */
    private Map < String, Integer > nextElementKeyInMapping;
    private int transactionLength;

    /**
     * This flag is true if the transaction is part of the positive database,
     * or false if it is part of the negative one
     */
    private boolean isPositive;

    public Transaction ( boolean isPositive ) {
        this.transactionMapping = new HashMap <>();
        this.nextElementKeyInMapping = new HashMap <>();
        this.transactionLength = 0;
        this.isPositive = isPositive;
    }

    /**
     * Adds a singular element into the transaction mapping structure
     *
     * @param element  element to be added
     * @param position index of the element to be added
     */
    public void addTransactionMapping ( String element, int position ) {
        HashMap < Integer, Integer > tempList;
        if ( transactionMapping.containsKey( element ) ) {
            tempList = transactionMapping.get( element );
            int tempPosition = nextElementKeyInMapping.remove( element );
            tempList.put( tempPosition, position );
            nextElementKeyInMapping.put( element, tempPosition + 1 );
        } else {
            tempList = new HashMap <>();
            tempList.put( 0, position );
            nextElementKeyInMapping.put( element, 1 );
            transactionMapping.put( element, tempList );
        }
        this.transactionLength++;
    }

    /**
     * Flushes the initialization support structure
     */
    public void flushInitializationSupportStructures () {
        this.nextElementKeyInMapping = null;
    }

    /**
     * Returns the next position of an element, if present
     * or throws an exception otherwise
     *
     * @param position                  index in the support structure requested
     * @param element                   singular item to be accessed
     * @param lastPositionInTransaction last visited position in the transaction
     * @return the next feasible position of the item
     * @throws NotPresentSymbolException if the item has not a feasible index, or if it is not present at all
     */
    public Integer getPosition ( int position, String element, int lastPositionInTransaction ) throws NotPresentSymbolException {
        try {
            int temp = transactionMapping.get( element ).get( position );
            if ( temp < lastPositionInTransaction ) {
                for ( int i = 1; true; i++ ) {
                    temp = transactionMapping.get( element ).get( position + i );
                    if ( temp > lastPositionInTransaction ) {
                        break;
                    }
                }
            }
            return temp;
        } catch ( IndexOutOfBoundsException | NullPointerException e ) {
            throw new NotPresentSymbolException();
        }

    }

    /**
     * Returns the nested hash map of a singular item that has incremental i
     * ntegers as keys and the actual symbol's positions as values.
     *
     * @param element singular items whose map is requested
     * @return the requested map
     * @throws NotPresentSymbolException if the map does not exist => the element is not present in the transaction
     */
    public HashMap < Integer, Integer > getElementMapping ( String element ) throws NotPresentSymbolException {
        HashMap < Integer, Integer > temp = transactionMapping.get( element );
        if ( temp != null ) {
            return temp;
        }
        throw new NotPresentSymbolException();
    }

    public boolean isPositive () {
        return isPositive;
    }
}
