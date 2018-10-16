package ECLAT;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import static Tools.Utilities.*;

public class ECLAT {
    private final int numberOfTransactions;
    private double minFrequency;

    public ECLAT ( int numberOfTransactions, double minFrequency ) {
        this.numberOfTransactions = numberOfTransactions;
        this.minFrequency = minFrequency;
    }

    public String starter(TreeMap<Integer, ArrayList<Integer> > verticalRepresentation){
        String toPrint="";
        for ( int lastPatternElement: verticalRepresentation.keySet()){
            double frequency = computeFrequency( verticalRepresentation.get( lastPatternElement ).size() );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement( lastPatternElement, frequency) );
            }
            TreeMap<Integer, ArrayList<Integer> > projectedVertical =
                    projectDatabase(verticalRepresentation, lastPatternElement);
            int [] patternList = { lastPatternElement };
            toPrint = toPrint.concat( starter( patternList, projectedVertical) );
        }
        return toPrint;
    }

    public String starter(int[] pattern, TreeMap<Integer, ArrayList<Integer> > verticalProjected){
        String toPrint="";

        for ( int lastPatternElement: verticalProjected.keySet()){
            double frequency = computeFrequency( verticalProjected.get( lastPatternElement ).size() );
            if( frequency >= minFrequency){
                toPrint = toPrint.concat( printElement(pattern, lastPatternElement, frequency) );
            }
            TreeMap<Integer, ArrayList<Integer> > projectedVertical =
                    projectDatabase(verticalProjected, lastPatternElement);
            int [] patternList = combine( pattern, lastPatternElement);
            toPrint = toPrint.concat( starter( patternList, projectedVertical) );
        }

        return toPrint;
    }

    private double computeFrequency(double lenght){
        return lenght/numberOfTransactions;
    }

    private TreeMap<Integer, ArrayList<Integer> > projectDatabase( TreeMap<Integer, ArrayList<Integer> > previousDatabase,
                                                                   int element){
        TreeMap<Integer, ArrayList<Integer>> projectedDatabase = new TreeMap <>();
        for ( Map.Entry<Integer, ArrayList<Integer>> entry : previousDatabase.entrySet()){
            //TODO
        }
        return null;
    }


}
