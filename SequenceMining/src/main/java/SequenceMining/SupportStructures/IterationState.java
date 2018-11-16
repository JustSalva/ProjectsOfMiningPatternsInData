package SequenceMining.SupportStructures;

import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the state of the iteration of a specific transaction
 * It acts as a projected database
 */
public class IterationState {

    /**
     * Index of the last element of the found pattern
     */
    private Integer indexForDirectAccess;

    /**
     * Index for each singular item that represents the last position visited
     * in the item's list in the projected database
     */
    private Map < String, Integer > lastTransactionIndexPerSymbol;

    public IterationState ( Integer indexForDirectAccess ) {
        this.indexForDirectAccess = indexForDirectAccess;
        this.lastTransactionIndexPerSymbol = new HashMap <>();
    }

    public IterationState ( IterationState iterationState ) {
        this.indexForDirectAccess = iterationState.indexForDirectAccess;
        this.lastTransactionIndexPerSymbol = new HashMap <>( iterationState.lastTransactionIndexPerSymbol );
    }

    public Integer getIndexForDirectAccess () {
        return indexForDirectAccess;
    }


    public Integer getLastTransactionIndexPerSymbol ( String symbol ) throws NotPresentSymbolException {
        if ( lastTransactionIndexPerSymbol.containsKey( symbol ) ) {
            return lastTransactionIndexPerSymbol.get( symbol );
        } else {
            return 0;
        }
    }

    /**
     * Updates the index of a visited symbol (singular item) with the first feasible occurrence of it in the
     * transaction, or creates its hash map entry if absent.
     * @param symbol symbol to be added
     * @param position position of the symbol in the projected database list
     * @param elementPositions actual positions of the symbol in the transaction
     */
    public void addLastTransactionIndexPerSymbol ( String symbol, Integer position,
                                                   HashMap < Integer, Integer > elementPositions ) {
        if ( lastTransactionIndexPerSymbol.containsKey( symbol ) ) {
            int previousPosition = lastTransactionIndexPerSymbol.get( symbol );
            this.lastTransactionIndexPerSymbol.put( symbol, previousPosition + 1 );
        } else {
            int i = 1;
            for ( Integer element : elementPositions.keySet() ) {
                if ( !elementPositions.get( element ).equals( position ) ) {
                    i++;
                } else {
                    break;
                }
            }
            this.lastTransactionIndexPerSymbol.put( symbol, i );
        }

        this.indexForDirectAccess = position;
    }

    /**
     * Updates the index of a visited symbol (singular item), or creates its hash map entry if absent.
     * @param symbol symbol to be added
     * @param position position of the symbol in the projected database list
     */
    public void addLastTransactionIndexPerSymbol ( String symbol, Integer position ) {
        if ( lastTransactionIndexPerSymbol.containsKey( symbol ) ) {
            int previousPosition = lastTransactionIndexPerSymbol.get( symbol );
            this.lastTransactionIndexPerSymbol.put( symbol, previousPosition + 1 );
        } else {
            this.lastTransactionIndexPerSymbol.put( symbol, 1 );
        }

        this.indexForDirectAccess = position;
    }
}
