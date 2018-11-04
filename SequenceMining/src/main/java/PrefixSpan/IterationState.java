package PrefixSpan;

import Exceptions.NotPresentSymbolException;

import java.util.HashMap;
import java.util.Map;

public class IterationState {
    private Integer indexForDirectAccess;
    private Map<String, Integer> lastTransactionIndexPerSymbol;

    public IterationState ( Integer indexForDirectAccess ) {
        this.indexForDirectAccess = indexForDirectAccess;
        this.lastTransactionIndexPerSymbol = new HashMap <>();
    }

    public IterationState ( IterationState iterationState ) {
        this.indexForDirectAccess = iterationState.indexForDirectAccess;
        this.lastTransactionIndexPerSymbol = new HashMap <>( iterationState.lastTransactionIndexPerSymbol);
    }

    public Integer getIndexForDirectAccess () {
        return indexForDirectAccess;
    }

    public void setIndexForDirectAccess ( Integer indexForDirectAccess ) {
        this.indexForDirectAccess = indexForDirectAccess;
    }

    public Integer getLastTransactionIndexPerSymbol (String symbol) throws NotPresentSymbolException {
        if(lastTransactionIndexPerSymbol.containsKey( symbol )){
            return lastTransactionIndexPerSymbol.get( symbol );
        }
        else{
            return  0;
        }
    }

    public void addLastTransactionIndexPerSymbol ( String symbol, Integer position) {
        if(lastTransactionIndexPerSymbol.containsKey( symbol )){
            int previousPosition = lastTransactionIndexPerSymbol.get( symbol );
            this.lastTransactionIndexPerSymbol.put( symbol, previousPosition + 1 );
        }else{
            this.lastTransactionIndexPerSymbol.put( symbol, 1 );
        }

        this.indexForDirectAccess = position;
    }
}
