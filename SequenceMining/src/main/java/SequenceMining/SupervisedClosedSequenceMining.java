package SequenceMining;


import SequenceMining.SupportStructures.CandidateClosedPattern;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Implements the closed pattern version of the Supervised sequence mining algorithm
 * that uses the Wracc function
 */
public class SupervisedClosedSequenceMining extends SupervisedSequenceMining {

    public SupervisedClosedSequenceMining ( int k ) {
        super( k );
    }

    public static void main ( String[] args ) {
        if ( args.length != 3 ) {
            System.out.println( "Incorrect number of arguments! Aborting execution." );
        } else {
            String filepathPositive = args[ 0 ];
            String filepathNegative = args[ 1 ];
            int k = Integer.parseInt( args[ 2 ] );
            GenericAlgorithm genericAlgorithm = new SupervisedClosedSequenceMining( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative, 5 ) );
        }

    }

    /**
     * {@inheritDoc}
     * This implementation, before printing the results removes all non closed patterns
     */
    @Override
    public String printResults ( int numberOfDecimals ) {
        String format = "%." + Integer.toString( numberOfDecimals ) + "f";
        LinkedHashMap < String, Float > result = getAllFoundPatterns().entrySet()
                .stream()
                .sorted( Collections.reverseOrder( Map.Entry.comparingByValue() ) )
                .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2 ) -> e2,
                        LinkedHashMap::new ) );
        int counter = 0;
        float previousFrequency = 0;
        String toPrint = "";
        ArrayList < CandidateClosedPattern > closedPatternsList = new ArrayList <>();
        ArrayList < CandidateClosedPattern > tempPatternList = new ArrayList <>();
        for ( Map.Entry < String, Float > entry : result.entrySet() ) {
            String key = entry.getKey();
            float nextFrequency = getAllFoundPatterns().get( key );
            if ( previousFrequency != nextFrequency ) {
                previousFrequency = nextFrequency;
                counter++;
                closedPatternsList.addAll( extractClosedItems( tempPatternList ) );
                tempPatternList.clear();

            }
            tempPatternList.add( new CandidateClosedPattern( getPositiveFoundPatterns().get( key ),
                    getNegativeFoundPatterns().get( key ), key ) );

            if ( counter > super.k ) {
                break;
            }
        }

        for ( CandidateClosedPattern pattern : closedPatternsList ) {
            float nextFrequency = getAllFoundPatterns().get( pattern.getPattern() );
            int positiveFrequency = pattern.getPositiveSupport();
            int negativeFrequency = pattern.getNegativeSupport();
            toPrint =
                    toPrint.concat( "[" + pattern.getPattern() + "]"
                            + " " + Integer.toString( positiveFrequency )
                            + " " + Integer.toString( negativeFrequency )
                            + " " + String.format( format, nextFrequency ) + "\n" );
        }
        return toPrint;

    }

    /**
     * Extracts from a list of patterns with same frequency the closed ones
     *
     * @param candidates patterns to be filtered
     * @return the patterns that are closed
     */
    ArrayList < CandidateClosedPattern > extractClosedItems ( ArrayList < CandidateClosedPattern > candidates ) {
        if ( candidates.size() > 1 ) {
            candidates = candidates
                    .stream()
                    .sorted( Comparator.comparingInt( CandidateClosedPattern::patternLength ) )
                    .collect( Collectors.toCollection( ArrayList::new ) );
            return recursivelyExtractClosedItems( candidates );
        }
        return candidates;

    }

    /**
     * Recursively selects the closed items and removes from the search
     * the sub-patterns that are not closed
     *
     * @param candidates patterns to be filtered
     * @return the patterns that are closed
     */
    private ArrayList < CandidateClosedPattern > recursivelyExtractClosedItems ( ArrayList < CandidateClosedPattern > candidates ) {
        boolean match = false;
        CandidateClosedPattern checkedElement = candidates.remove( 0 );
        if ( candidates.isEmpty() ) {
            candidates.add( checkedElement );
            return candidates;
        }
        for ( CandidateClosedPattern element : candidates ) {
            if ( patternsMatch( checkedElement, element ) ) {
                match = true;
                break;
            }
        }
        if ( match ) {
            return recursivelyExtractClosedItems( candidates );
        } else {
            ArrayList < CandidateClosedPattern > toReturn = recursivelyExtractClosedItems( candidates );
            toReturn.add( checkedElement );
            return toReturn;
        }
    }

    /**
     * Checks if the first pattern is contained in the second (longest) one
     *
     * @param shorterElement shorter pattern to be checked
     * @param longerElement  longer pattern to be checked
     * @return true if the first pattern is a sub-sequence of teh second, false otherwise
     */
    private boolean patternsMatch ( CandidateClosedPattern shorterElement, CandidateClosedPattern longerElement ) {
        int lastMatchPosition = -1;
        int matchPosition;
        for ( String item : shorterElement.getPattern().split( ", " ) ) {
            matchPosition = longerElement.getPattern().indexOf( item, lastMatchPosition + 1 );
            if ( !( matchPosition > lastMatchPosition ) ) {
                return false;
            }
            lastMatchPosition = matchPosition;
        }
        return true;
    }
}
