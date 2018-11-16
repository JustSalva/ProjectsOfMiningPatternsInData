package SequenceMining;


import java.util.*;

import static java.util.stream.Collectors.toMap;

public class SupervisedClosedSequenceMining extends SupervisedSequenceMining {

    protected Map<String, HashMap< Integer, IterationState >> closedItemsIterationStates;

    public SupervisedClosedSequenceMining ( int k ) {
        super( k );
        this.closedItemsIterationStates = new HashMap <>();
    }

    @Override
    void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative,
                            HashMap< Integer, IterationState > transactionStartingPosition ) {
        super.addToPatternList( pattern, patternSupportPositive, patternSupportNegative, transactionStartingPosition );
        closedItemsIterationStates.put( pattern,  transactionStartingPosition);

    }

    @Override
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
        HashMap<Integer, String> closedItems = new HashMap <>();
        ArrayList<String> closedPatternsList = new ArrayList <>();
        for(Map.Entry<String, Float> entry : result.entrySet()){
            String key = entry.getKey();
            float nextFrequency = allFoundPatterns.get( key );
            if(previousFrequency != nextFrequency){
                previousFrequency = nextFrequency;
                counter++;
                closedPatternsList.addAll( closedItems.values());
                closedItems = new HashMap <>();

            }
            int closeValue = computeCloseValue( key );

            if(closedItems.containsKey( closeValue )){
               String oldPattern = closedItems.get( closeValue );
               if(key.length() > oldPattern.length() ){
                   closedItems.put( closeValue, key );
               }
            }else{
                closedItems.put( closeValue, key );
            }

            if (counter > super.k){
                break;
            }
        }

        for(String pattern: closedPatternsList){
            float nextFrequency = allFoundPatterns.get( pattern );
            int positiveFrequency = positiveFoundPatterns.get( pattern );
            int negativeFrequency = negativeFoundPatterns.get( pattern );
            toPrint =
                    toPrint.concat( "[" + pattern +"]"
                            + " " + Integer.toString( positiveFrequency)
                            + " " + Integer.toString( negativeFrequency)
                            + " " + String.format(format, nextFrequency ) + "\n");
        }
        return toPrint;

    }

    private Integer computeCloseValue( String pattern ){
        HashMap< Integer, IterationState > transactionStates= closedItemsIterationStates.get( pattern );
        //int cardinality = computeCardinality(transactionStates);
        int finalValue = 0;
        for( Map.Entry<Integer,IterationState> iterationStateEntry: transactionStates.entrySet()){

            finalValue += transactions.get( iterationStateEntry.getKey() ).getTransactionLength() - iterationStateEntry.getValue().getIndexForDirectAccess() + 1;
        }
        return finalValue;
    }

    private int computeCardinality( HashMap< Integer, IterationState > transactionStates ){
        return transactionStates.size();
    }



    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            GenericAlgorithm genericAlgorithm = new SupervisedClosedSequenceMining( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative ,5 ) );
        }

    }
}
