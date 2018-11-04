package PrefixSpan;

import DataSet.DatasetPrefixSpan;
import Exceptions.NotPresentSymbolException;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class PrefixSpan {

    private Map<String, Integer> positiveFoundPatterns;
    private Map<String, Integer> negativeFoundPatterns;
    private ArrayList<String> items; //The different items in the dataset
    private ArrayList< TransactionPrefixSpan > transactions;
    private int k;

    public PrefixSpan ( int k) {
        this.positiveFoundPatterns = new HashMap <>();
        this.negativeFoundPatterns = new HashMap <>();
        this.k = k;
    }

    public String start(String filepathPositive, String filepathNegative){
        initializeDataset( filepathPositive );
        start( positiveFoundPatterns);
        initializeDataset( filepathNegative );
        start( negativeFoundPatterns );
        return printResults();
    }

    public void initializeDataset(String filePath){
        DatasetPrefixSpan dataset = new DatasetPrefixSpan( filePath );
        this.items = dataset.getItems();
        this.transactions = dataset.getTransactions();
    }

    public void start(Map<String, Integer> foundPatterns){
        HashMap<Integer, IterationState> transactionStartingPosition;
        IterationState iterationState;
        int position;
        for(String item : items){
            int patternSupport = 0;
            transactionStartingPosition = new HashMap <>();

            for(int i = 0; i< transactions.size(); i++){

                try {
                    position = transactions.get( i ).getPosition( 0, item , 0);
                    iterationState = new IterationState( position );
                    iterationState.addLastTransactionIndexPerSymbol( item,  position);
                    transactionStartingPosition.put( i, iterationState );
                    patternSupport ++;
                } catch ( NotPresentSymbolException e ) {
                    continue;
                }
            }
            foundPatterns.put( item, patternSupport );
            start( item, transactionStartingPosition, foundPatterns);
        }

    }

    public void start(String pattern, HashMap<Integer, IterationState> transactionStartingPosition, Map<String,
            Integer> foundPatterns){
        HashMap<Integer, IterationState> newTransactionStartingPosition;
        IterationState iterationState;
        int position;
        for(String item : items){
            int patternSupport = 0;
            newTransactionStartingPosition = new HashMap <>();

            for(int transactionNumber : transactionStartingPosition.keySet()){

                try {
                    position =
                            transactions.get( transactionNumber ).getPosition(
                                    transactionStartingPosition.get( transactionNumber )
                                            .getLastTransactionIndexPerSymbol( item ) , item , transactionStartingPosition.get( transactionNumber ).getIndexForDirectAccess());
                    if(position <= transactionStartingPosition.get( transactionNumber ).getIndexForDirectAccess()){
                        continue;
                    }
                    iterationState = new IterationState( transactionStartingPosition.get( transactionNumber ) );
                    iterationState.addLastTransactionIndexPerSymbol( item,  position);
                    newTransactionStartingPosition.put( transactionNumber, iterationState );
                    patternSupport ++;
                } catch ( NotPresentSymbolException e ) {
                    continue;
                }
            }
            if( patternSupport > 0){
                foundPatterns.put( pattern.concat( ", " + item ), patternSupport );
                start( pattern.concat( ", " + item ), newTransactionStartingPosition, foundPatterns);
            }

        }
    }

    public String printResults(){
        Map<String, Integer> allFoundPatterns = new HashMap <>( positiveFoundPatterns );
        int temp_value;
        for( String key : negativeFoundPatterns.keySet()){
            if(allFoundPatterns.containsKey( key )){
                temp_value = allFoundPatterns.get( key );
                allFoundPatterns.put( key, temp_value + negativeFoundPatterns.get( key ) );
            }else{
                allFoundPatterns.put( key, negativeFoundPatterns.get( key ) );
            }
        }
        LinkedHashMap<String, Integer> result = allFoundPatterns.entrySet()
                .stream()
                .sorted( Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect( toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        int counter = 0;
        int previousFrequency = 0;
        String toPrint = "";
        for(Map.Entry<String, Integer> entry : result.entrySet()){
            String key = entry.getKey();
            int nextFrequency = allFoundPatterns.get( key );
            int positiveFrequency;
            try{
                positiveFrequency = positiveFoundPatterns.get( key );
            }catch ( NullPointerException e ){
                positiveFrequency = 0;
            }
            int negativeFrequency;
            try{
                negativeFrequency = negativeFoundPatterns.get( key );
            }catch ( NullPointerException e ){
                negativeFrequency = 0;
            }
            toPrint =
                    toPrint.concat( "[" + key +"]"
                            + " " + Integer.toString( positiveFrequency)
                            + " " + Integer.toString( negativeFrequency)
                            + " " + Integer.toString( nextFrequency ) + "\n");

            if(previousFrequency != nextFrequency){
                previousFrequency = nextFrequency;
                counter++;
            }
            if (counter >=k){
                break;
            }
        }
        return toPrint;

    }

    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            PrefixSpan prefixSpan = new PrefixSpan( k );
            System.out.print( prefixSpan.start( filepathPositive, filepathNegative ) );
        }

    }
}
