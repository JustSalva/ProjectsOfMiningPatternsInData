package SequenceMining;

import SequenceMining.DataSet.Dataset;
import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public abstract class GenericAlgorithm {

    protected Map<String, Float> positiveFoundPatterns;
    protected Map<String, Float> negativeFoundPatterns;
    protected ArrayList<String> items; //The different items in the dataset
    protected ArrayList< Transaction > transactions;
    protected Map<String, Float> allFoundPatterns;
    protected int kCounter;

    private int k;

    public GenericAlgorithm ( int k) {
        this.positiveFoundPatterns = new HashMap <>();
        this.negativeFoundPatterns = new HashMap <>();
        this.k = k;
        this.allFoundPatterns = new HashMap <>();
        this.kCounter = 0;
    }

    public String start(String filepathPositive, String filepathNegative, int numberOfDecimals){
        initializeDataset( filepathPositive, filepathNegative);
        start();
        return printResults(numberOfDecimals);
    }

    private void initializeDataset(String filePathPositive, String filePathNegative){

        Dataset dataset = new Dataset( filePathPositive, true );
        HashSet<String> positiveItems = dataset.getItems();
        this.transactions = dataset.getTransactions();

        dataset = new Dataset( filePathNegative , false );
        transactions.addAll( dataset.getTransactions() );

        joinItems( positiveItems, dataset.getItems() );
    }

    private void joinItems(HashSet<String> positiveItems, HashSet<String> negativeItems){
        positiveItems.addAll( negativeItems );
        this.items = new ArrayList <>( positiveItems );
        Collections.sort( this.items );
    }

    public void start(){
        HashMap< String, HashMap<Integer, IterationState>> firstRecursionNodes = new HashMap <>();
        HashMap<Integer, IterationState> transactionStartingPosition;
        IterationState iterationState;
        int position;
        for(String item : items){
            Float patternSupportPositive = 0f;
            Float patternSupportNegative = 0f;
            transactionStartingPosition = new HashMap <>();

            for(int i = 0; i< transactions.size(); i++){

                try {
                    position = transactions.get( i ).getPosition( 0, item , 0);
                    iterationState = new IterationState( position );
                    iterationState.addLastTransactionIndexPerSymbol( item,  position);
                    transactionStartingPosition.put( i, iterationState );
                    if(transactions.get( i ).isPositive()){
                        patternSupportPositive ++;
                    }else{
                        patternSupportNegative ++;
                    }
                } catch ( NotPresentSymbolException e ) {
                    continue;
                }
            }
            addToPatternList( item, patternSupportPositive, patternSupportNegative);
            if(constraintsAreMetInFirstLevel(item, patternSupportPositive, patternSupportNegative)) {
                firstRecursionNodes.put( item, transactionStartingPosition );

            }
            for(String pattern: firstRecursionNodes.keySet()){
                start( pattern, firstRecursionNodes.get( pattern ) );
            }
        }

    }

    public void start(String pattern, HashMap<Integer, IterationState> transactionStartingPosition){
        HashMap<Integer, IterationState> newTransactionStartingPosition;
        IterationState iterationState;
        int position;
        for(String item : items){
            Float patternSupportPositive = 0f;
            Float patternSupportNegative = 0f;
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
                    iterationState.addLastTransactionIndexPerSymbol( item,  position,
                            transactions.get( transactionNumber ).getElementMapping( item ));
                    newTransactionStartingPosition.put( transactionNumber, iterationState );
                    if(transactions.get( transactionNumber ).isPositive()){
                        patternSupportPositive ++;
                    }else{
                        patternSupportNegative ++;
                    }
                } catch ( NotPresentSymbolException e ) {
                    continue;
                }
            }
            if( patternSupportPositive > 0 || patternSupportNegative >0 ){
                String temp = pattern.concat( ", " + item );
                addToPatternList( temp, patternSupportPositive, patternSupportNegative);
                if(constraintsAreMet(pattern, temp, patternSupportPositive, patternSupportNegative)) {
                    start( pattern.concat( ", " + item ), newTransactionStartingPosition);
                }
            }

        }
    }

    private boolean constraintsAreMet(String fatherPattern, String pattern, Float patternSupportPositive,
                                      Float patternSupportNegative){
        if( kCounter<k ){

            addMinElement(pattern, patternSupportPositive, patternSupportNegative);
            return true;
        }
        else{
            return checkConstraints(fatherPattern, pattern, patternSupportPositive, patternSupportNegative);
        }
    }
    private boolean constraintsAreMetInFirstLevel(String pattern, Float patternSupportPositive,
                                                  Float patternSupportNegative){
        if( kCounter<k ){

            addMinElement(pattern, patternSupportPositive, patternSupportNegative);
            return true;
        }
        else{
            return checkConstraintsInFirstLevel(pattern, patternSupportPositive, patternSupportNegative);
        }
    }

    abstract boolean checkConstraintsInFirstLevel( String pattern, Float patternSupportPositive,
                                                   Float patternSupportNegative);
    abstract void addMinElement(String pattern, Float patternSupportPositive, Float patternSupportNegative);
    abstract boolean checkConstraints(String fatherPattern, String pattern, Float patternSupportPositive,
                                      Float patternSupportNegative);


    abstract void addToPatternList(String pattern, Float patternSupportPositive, Float patternSupportNegative);


    public String printResults(int numberOfDecimals){
        String printFormat = "%."+Integer.toString( numberOfDecimals )+"f";
        LinkedHashMap<String, Float> result = allFoundPatterns.entrySet()
                .stream()
                .sorted( Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect( toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        int counter = 0;
        float previousFrequency = 0;
        String toPrint = "";
        for(Map.Entry<String, Float> entry : result.entrySet()){
            String key = entry.getKey();

            float nextFrequency = allFoundPatterns.get( key );
            float positiveFrequency = positiveFoundPatterns.get( key );
            float negativeFrequency = negativeFoundPatterns.get( key );

            if(previousFrequency != nextFrequency){
                previousFrequency = nextFrequency;
                counter++;
            }
            if (counter > k){
                break;
            }
            else{
                toPrint =
                        toPrint.concat( "[" + key +"]"
                                + " " + String.format(printFormat, positiveFrequency)
                                + " " + String.format(printFormat,  negativeFrequency)
                                + " " + String.format(printFormat, nextFrequency ) + "\n");
            }
        }
        return toPrint;

    }


}
