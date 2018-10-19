package ECLAT;

import java.util.Map;
import java.util.TreeMap;
import static Tools.Utilities.*;

public class ECLAT {
    private final int numberOfTransactions;
    private double minFrequency;
    private double frequency; //temp variable to optimize

    public ECLAT ( int numberOfTransactions, double minFrequency ) {
        this.numberOfTransactions = numberOfTransactions - 1;
        this.minFrequency = minFrequency;
    }

    public String starter(TreeMap<Integer, int[] > verticalRepresentation){
        String toPrint="";
        TreeMap<Integer, int[] > verticalProjectedCopy = new TreeMap <>( verticalRepresentation );
        int [] patternList;
        for ( int lastPatternElement : verticalRepresentation.keySet()){
            this.frequency = computeFrequency( verticalRepresentation.get( lastPatternElement ).length);
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement( lastPatternElement, frequency) );
            }
            TreeMap<Integer, int[] > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            patternList = new int[]{ lastPatternElement };
            toPrint = toPrint.concat( starter( patternList, projectedVertical) );
        }
        return toPrint;
    }

    public String starter(int[] pattern, TreeMap<Integer, int[] > verticalProjected){
        String toPrint="";
        TreeMap<Integer, int[] > verticalProjectedCopy = new TreeMap <>( verticalProjected );
        int[] patternList;
        for ( int lastPatternElement : verticalProjected.keySet()){
            this.frequency = computeFrequency( verticalProjected.get( lastPatternElement ).length );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement(pattern, lastPatternElement, frequency) );
            }
            TreeMap<Integer, int[] > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            patternList = combine( pattern, lastPatternElement);
            if(projectedVertical.size() > 0){
                toPrint = toPrint.concat( starter( patternList, projectedVertical) );
            }
        }

        return toPrint;
    }

    private double computeFrequency(double length){
        return length/numberOfTransactions;
    }

    private TreeMap<Integer, int[] > projectDatabase( TreeMap<Integer, int[] > previousDatabase, int element){

        TreeMap<Integer, int[]> projectedDatabase = new TreeMap <>();
        TreeMap<Integer, int[]> previousDatabaseCopy = new TreeMap <>(previousDatabase);
        int[] pattern = previousDatabase.get( element );
        previousDatabaseCopy.remove( element );
        boolean singleMatch; //match of one single item
        boolean fullMatch; //match if the entire transaction (== at least one match)
        int transactionIndex = 0;
        int patternIndex;
        int[] temp = null;
        int[] transaction;
        for ( Map.Entry<Integer, int[]> entry : previousDatabaseCopy.entrySet()) {
            transaction = entry.getValue();
            patternIndex = 0;
            fullMatch = false;
            transactionIndex = 0;
            while ( patternIndex < pattern.length) {
                singleMatch = false;
                if( pattern[patternIndex] >= transaction[transactionIndex]){

                    while ( transactionIndex < transaction.length ) {
                        if ( pattern[patternIndex] == ( transaction[transactionIndex] ) ) {
                            singleMatch = true;
                            transactionIndex++;
                            break;
                        }
                        transactionIndex++;
                    }
                    if ( singleMatch ) {
                        temp = combine( temp, pattern[patternIndex] );
                        fullMatch = true;
                    }
                    else if( transactionIndex == transaction.length
                            && pattern[patternIndex] != ( transaction[transactionIndex-1] )) {
                        transactionIndex = 0;
                    }
                    if ( transactionIndex == transaction.length && pattern[patternIndex] == ( transaction[transactionIndex-1] )){
                        break;
                    }
                }
                patternIndex++;
            }
            if ( fullMatch ) {
                projectedDatabase.put( entry.getKey(), temp.clone() );
                temp = null;
            }

        }
        return projectedDatabase;
    }


}
