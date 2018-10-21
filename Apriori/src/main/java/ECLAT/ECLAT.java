package ECLAT;

import java.util.*;

import static Tools.Utilities.*;

public class ECLAT {
    private final int numberOfTransactions;
    private double minFrequency;
    private double frequency; //temp variable to optimize
    private double minTransactionLenght;

    public ECLAT ( int numberOfTransactions, double minFrequency ) {
        this.numberOfTransactions = numberOfTransactions - 1;
        this.minFrequency = minFrequency;
        this.minTransactionLenght = Math.floor( minFrequency * numberOfTransactions);
    }

    public String starter(TreeMap<Integer, Set<Integer> > verticalRepresentation){
        String toPrint="";
        TreeMap<Integer, Set<Integer> > verticalProjectedCopy = new TreeMap <>( verticalRepresentation );
        Set <Integer> patternList;
        for ( int lastPatternElement : verticalRepresentation.keySet()){
            this.frequency = computeFrequency( verticalRepresentation.get( lastPatternElement ).size());
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement( lastPatternElement, frequency) );
            }
            TreeMap<Integer, Set<Integer> > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            patternList = new LinkedHashSet<Integer>() ;
            patternList.add( lastPatternElement );
            toPrint = toPrint.concat( starter( patternList, projectedVertical) );
        }
        return toPrint;
    }

    public String starter(Set<Integer> pattern, TreeMap<Integer, Set<Integer> > verticalProjected){
        String toPrint="";
        TreeMap<Integer, Set<Integer> > verticalProjectedCopy = new TreeMap <>( verticalProjected );
        Set<Integer> patternList;
        for ( int lastPatternElement : verticalProjected.keySet()){
            this.frequency = computeFrequency( verticalProjected.get( lastPatternElement ).size() );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement(pattern, lastPatternElement, frequency) );
            }
            TreeMap<Integer, Set<Integer> > projectedVertical =
                    projectDatabase(verticalProjectedCopy, lastPatternElement);
            verticalProjectedCopy.remove( lastPatternElement );
            patternList = new LinkedHashSet<Integer>(pattern);
            patternList.add( lastPatternElement);
            if(projectedVertical.size() > 0){
                toPrint = toPrint.concat( starter( patternList, projectedVertical) );
            }
        }

        return toPrint;
    }

    private double computeFrequency(double length){
        return length/numberOfTransactions;
    }

    private TreeMap<Integer, Set<Integer> > projectDatabase( TreeMap<Integer, Set<Integer> > previousDatabase, int element){

        TreeMap<Integer, Set<Integer>> projectedDatabase = new TreeMap <>();
        TreeMap<Integer, Set<Integer>> previousDatabaseCopy = new TreeMap <>(previousDatabase);
        Set<Integer> pattern = previousDatabase.get( element );
        previousDatabaseCopy.remove( element );
        boolean fullMatch = false; //match if the entire transaction (== at least one match)
        Set<Integer> temp = null;
        Set<Integer> transaction = null;
        for ( Map.Entry<Integer, Set<Integer>> entry : previousDatabaseCopy.entrySet()) {
            transaction = entry.getValue();
            if(transaction.size() >= minTransactionLenght){ //no enough frequency is possible
                if( transaction.size() > pattern.size()){
                    temp = performAND( transaction, pattern );
                }else{
                    temp = performAND( pattern, transaction );
                }
                if ( temp != null ) {
                    if(temp.size() >= minTransactionLenght){
                        projectedDatabase.put( entry.getKey(), temp );
                    }
                    temp = null;
                    fullMatch = false;
                }
            }



        }
        return projectedDatabase;
    }

    private Set<Integer> performAND(Set<Integer> biggerSet, Set<Integer> smallerSet){
        Set<Integer> temp = null;
        for(int patternElement : smallerSet){
            if(biggerSet.contains( patternElement )){
                if(temp == null) {
                    temp = new LinkedHashSet<>();
                }
                temp.add( patternElement );
            }
        }
        return temp;
    }
}
