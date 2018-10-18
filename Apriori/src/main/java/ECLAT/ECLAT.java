package ECLAT;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import static Tools.Utilities.*;

public class ECLAT {
    private final int numberOfTransactions;
    private double minFrequency;

    public ECLAT ( int numberOfTransactions, double minFrequency ) {
        this.numberOfTransactions = numberOfTransactions - 1;
        this.minFrequency = minFrequency;
    }

    public String starter(TreeMap<Integer, ArrayList<Integer> > verticalRepresentation){
        String toPrint="";
        TreeMap<Integer, ArrayList<Integer> > verticalProjectedCopy = new TreeMap <>( verticalRepresentation );
        for ( int lastPatternElement : verticalRepresentation.keySet()){
            double frequency = computeFrequency( verticalRepresentation.get( lastPatternElement ).size() );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement( lastPatternElement, frequency) );
            }
            TreeMap<Integer, ArrayList<Integer> > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            int [] patternList = { lastPatternElement };
            toPrint = toPrint.concat( starter( patternList, projectedVertical) );
        }
        return toPrint;
    }

    public String starter(int[] pattern, TreeMap<Integer, ArrayList<Integer> > verticalProjected){
        String toPrint="";
        TreeMap<Integer, ArrayList<Integer> > verticalProjectedCopy = new TreeMap <>( verticalProjected );
        for ( int lastPatternElement : verticalProjected.keySet()){
            double frequency = computeFrequency( verticalProjected.get( lastPatternElement ).size() );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement(pattern, lastPatternElement, frequency) );
            }
            TreeMap<Integer, ArrayList<Integer> > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            int [] patternList = combine( pattern, lastPatternElement);
            if(projectedVertical.size() > 0){
                toPrint = toPrint.concat( starter( patternList, projectedVertical) );
            }
        }

        return toPrint;
    }

    private double computeFrequency(double length){
        return length/numberOfTransactions;
    }

    private TreeMap<Integer, ArrayList<Integer> > projectDatabase( TreeMap<Integer, ArrayList<Integer> > previousDatabase,
                                                                   int element){
        TreeMap<Integer, ArrayList<Integer>> projectedDatabase = new TreeMap <>();
        TreeMap<Integer, ArrayList<Integer>> previousDatabaseCopy = new TreeMap <>(previousDatabase);
        ArrayList<Integer> pattern = previousDatabase.get( element );
        previousDatabaseCopy.remove( element );
        boolean singleMatch; //match of one single item
        boolean fullMatch; //match if the entire transaction (== at least one match)
        int transactionIndex = 0;
        int patternIndex;
        ArrayList <Integer> temp = new ArrayList <>();
        for ( Map.Entry<Integer, ArrayList<Integer>> entry : previousDatabaseCopy.entrySet()) {
            //if ( entry.getKey() > element ) {
                patternIndex = 0;
                fullMatch = false;
                transactionIndex = 0;
                while ( patternIndex < pattern.size() ) {
                    transactionIndex = 0; //TODO maybe we can optimize this
                    singleMatch = false;

                    while ( transactionIndex < entry.getValue().size() ) {
                        if ( pattern.get( patternIndex ).equals( entry.getValue().get( transactionIndex ) ) ) {
                            singleMatch = true;
                            transactionIndex++;
                            break;
                        }
                        transactionIndex++;
                    }
                    if ( singleMatch ) {
                        temp.add( pattern.get( patternIndex ) );
                        fullMatch = true;
                    }
                    patternIndex++;
                }
                if ( fullMatch ) {
                    projectedDatabase.put( entry.getKey(), new ArrayList <>( temp ) );
                    temp.clear();
                }
           // }
        }
        return projectedDatabase;
    }


}
