package SequenceMining;

import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private Map<String, HashMap<Integer, Integer> > transactionMapping;
    private Map<String, Integer> nextElementKeyInMapping;
    private int transactionLength;
    private boolean isPositive;

    public Transaction ( boolean isPositive ) {
        this.transactionMapping = new HashMap <>();
        this.nextElementKeyInMapping = new HashMap <>();
        this.transactionLength = 0;
        this.isPositive = isPositive;
    }

    public Map < String, HashMap<Integer, Integer> > getTransactionMapping () {
        return transactionMapping;
    }

    public void addTransactionMapping ( String element, int position ) {
        HashMap<Integer, Integer> tempList;
        if(transactionMapping.containsKey( element )){
            tempList = transactionMapping.get( element );
            int tempPosition = nextElementKeyInMapping.remove( element ) ;
            tempList.put(tempPosition, position );
            nextElementKeyInMapping.put( element, tempPosition +1);
        }
        else{
            tempList = new HashMap<>();
            tempList.put(0, position );
            nextElementKeyInMapping.put( element, 1);
            transactionMapping.put( element, tempList );
        }
        this.transactionLength ++;
    }

    public void flushInitializationSupportStructures(){
        this.nextElementKeyInMapping = null;
    }

    public int getTransactionLength () {
        return transactionLength;
    }

    public boolean isElementPresent( String element ){
        return transactionMapping.containsKey( element );
    }

    public Integer getPosition( int position, String element, int lastPositionInTransaction) throws NotPresentSymbolException {
        try {
            int temp = transactionMapping.get( element ).get( position );
            if(temp < lastPositionInTransaction){
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
    public  HashMap<Integer, Integer>  getElementMapping (String element) throws NotPresentSymbolException{
        HashMap<Integer, Integer> temp = transactionMapping.get( element );
        if( temp != null){
            return temp;
        }
        throw new NotPresentSymbolException();
    }

    public boolean isPositive () {
        return isPositive;
    }
}
