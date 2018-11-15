package SequenceMining;

import SequenceMining.DataSet.Dataset;
import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public abstract class GenericAlgorithm{

    protected Map<String, Integer> positiveFoundPatterns;
    protected Map<String, Integer> negativeFoundPatterns;
    protected LinkedHashMap<String, Integer> items; //The different items in the dataset
    protected HashMap< Integer, Transaction > transactions;
    protected Map<String, Float> allFoundPatterns;
    protected int kCounter;
    private PriorityQueue<NodeToExpand> nodeToExpand;
    private int k;

    public GenericAlgorithm ( int k) {
        this.positiveFoundPatterns = new HashMap <>();
        this.negativeFoundPatterns = new HashMap <>();
        this.k = k;
        this.allFoundPatterns = new HashMap <>();
        this.kCounter = 0;
        this.nodeToExpand = new PriorityQueue <>();
        this.transactions = new HashMap <>();
    }

    public String start(String filepathPositive, String filepathNegative, int numberOfDecimals){
        initializeDataset( filepathPositive, filepathNegative);
        start();
        return printResults(numberOfDecimals);
    }

    protected void initializeDataset(String filePathPositive, String filePathNegative){

        Dataset dataset = new Dataset( filePathPositive, true );
        LinkedHashMap<String, Integer> positiveItems = dataset.getItems();
        ArrayList < Transaction > transactions = dataset.getTransactions();

        dataset = new Dataset( filePathNegative , false );
        transactions.addAll( dataset.getTransactions() );
        for( int i=0; i < transactions.size(); i++ ){
            this.transactions.put( i, transactions.get( i ) );
        }

        joinItems( positiveItems, dataset.getItems() );
    }

    protected void joinItems(LinkedHashMap<String, Integer> positiveItems, LinkedHashMap<String, Integer> negativeItems){
        for( Map.Entry<String, Integer> entry : negativeItems.entrySet() ){
            if(positiveItems.containsKey( entry.getKey() )){
                int temp = positiveItems.get( entry.getKey() );
                positiveItems.put( entry.getKey(), temp+entry.getValue() );
            }else{
                positiveItems.put( entry.getKey(), entry.getValue() );
            }
        }
        this.items = positiveItems.entrySet().stream()
                .sorted( Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2) -> e2, LinkedHashMap::new));
    }

    public void start(){
        //HashMap< String, HashMap<Integer, IterationState>> firstRecursionNodes = new HashMap <>();
        HashMap<Integer, IterationState> transactionStartingPosition;
        IterationState iterationState;
        int position;
        HashSet<String> newItems = new HashSet <>();
        for(String item: items.keySet()){
            int patternSupportPositive = 0;
            int patternSupportNegative = 0;
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
                nodeToExpand.add( new NodeToExpand( item, transactionStartingPosition, allFoundPatterns.get( item ) ) );
                newItems.add( item );
            }

        }
        HashSet<String> finalItems = new HashSet <>();
        for(String item: newItems){
           if(isStillToBeExpanded( allFoundPatterns.get( item ))){
               finalItems.add( item );
           }
        }

        int length = nodeToExpand.size();
        for(int i= 0; i<length; i++){
            NodeToExpand nextNode = nodeToExpand.poll();
            start( nextNode.getPattern(), nextNode.getTransactionStartingPosition() , finalItems);
        }



    }

    public void start(String pattern, HashMap<Integer, IterationState> transactionStartingPosition, HashSet<String> items){
        HashMap<Integer, IterationState> newTransactionStartingPosition;

        IterationState iterationState;
        int position;
        boolean itemFound;
        if(isStillToBeExpanded( allFoundPatterns.get( pattern ) )){
            HashSet<String> newItems = new HashSet <>( items );
            for(String item : items){
                Integer patternSupportPositive = 0;
                Integer patternSupportNegative = 0;
                newTransactionStartingPosition = new HashMap <>();
                itemFound = false;
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
                        itemFound = true;
                    } catch ( NotPresentSymbolException e ) {
                        continue;
                    }
                }
                if(itemFound == false){
                    newItems.remove( item );
                }
                if( patternSupportPositive > 0 || patternSupportNegative >0 ){
                    String temp = pattern.concat( ", " + item );
                    addToPatternList( temp, patternSupportPositive, patternSupportNegative);
                    if(constraintsAreMet(pattern, temp, patternSupportPositive, patternSupportNegative)) {
                        nodeToExpand.add( new NodeToExpand( temp, newTransactionStartingPosition,
                                allFoundPatterns.get( temp ) ) );
                        if(!nodeToExpand.isEmpty()){
                            NodeToExpand nextNode = nodeToExpand.poll();

                            start( nextNode.getPattern(), nextNode.getTransactionStartingPosition(),
                                    new HashSet <>( newItems ) );
                        }

                    }
                }

            }
        }
    }

    private boolean constraintsAreMet(String fatherPattern, String pattern, Integer patternSupportPositive,
                                      Integer patternSupportNegative){
        if( kCounter<k ){

            addMinElement(pattern, patternSupportPositive, patternSupportNegative);
            return true;
        }
        else{
            return checkConstraints(fatherPattern, pattern, patternSupportPositive, patternSupportNegative);
        }
    }
    private boolean constraintsAreMetInFirstLevel(String pattern, Integer patternSupportPositive,
                                      Integer patternSupportNegative){
        if( kCounter<k ){

            addMinElement(pattern, patternSupportPositive, patternSupportNegative);
            return true;
        }
        else{
            return checkConstraintsInFirstLevel(pattern, patternSupportPositive, patternSupportNegative);
        }
    }

    abstract boolean checkConstraintsInFirstLevel( String pattern, Integer patternSupportPositive,
                                      Integer patternSupportNegative);
    abstract void addMinElement(String pattern, Integer patternSupportPositive, Integer patternSupportNegative);
    abstract boolean checkConstraints(String fatherPattern, String pattern, Integer patternSupportPositive,
                                      Integer patternSupportNegative);
    abstract boolean isStillToBeExpanded(Float score);

    abstract void addToPatternList(String pattern, Integer patternSupportPositive, Integer patternSupportNegative);


    public String printResults(int numberOfDecimals){
        String format = "%." + Integer.toString( numberOfDecimals ) + "f";
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
            int positiveFrequency = positiveFoundPatterns.get( key );
            int negativeFrequency = negativeFoundPatterns.get( key );

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
                                + " " + Integer.toString( positiveFrequency)
                                + " " + Integer.toString( negativeFrequency)
                                + " " + String.format(format, nextFrequency ) + "\n");
            }
        }
        return toPrint;

    }


}
