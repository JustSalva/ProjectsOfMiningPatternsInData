package PrefixSpan;

import Exceptions.NotPresentSymbolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionPrefixSpan {
    private Map<String, ArrayList<Integer> > transactionMapping;
    private int transactionLength;

    public TransactionPrefixSpan () {
        this.transactionMapping = new HashMap <>();
        this.transactionLength = 0;
    }

    public Map < String, ArrayList < Integer > > getTransactionMapping () {
        return transactionMapping;
    }

    public void addTransactionMapping ( String element, int position ) {
        ArrayList<Integer> tempList;
        if(transactionMapping.containsKey( element )){
            tempList = transactionMapping.get( element );
            tempList.add( position );
        }
        else{
            tempList = new ArrayList <>();
            tempList.add( position );
            transactionMapping.put( element, tempList );
        }
        this.transactionLength ++;
    }

    public int getTransactionLength () {
        return transactionLength;
    }

    public boolean isElementPresent( String element ){
        return transactionMapping.containsKey( element );
    }

    public Integer getPosition( int position, String element, int lastPositionInTransaction) throws NotPresentSymbolException {
        try {
            //TODO
            int temp = transactionMapping.get( element ).get( position );
            if(temp <= lastPositionInTransaction){
                for(int i=1;true; i++){
                    temp = transactionMapping.get( element ).get( position +i );
                    if(temp > lastPositionInTransaction){
                        break;
                    }
                }
            }
            return temp;
        }catch ( IndexOutOfBoundsException | NullPointerException e  ){
            throw new NotPresentSymbolException();
        }

    }
}
