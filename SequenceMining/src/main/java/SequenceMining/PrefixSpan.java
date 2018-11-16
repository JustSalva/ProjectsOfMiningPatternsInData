package SequenceMining;

import java.util.Collections;
import java.util.TreeSet;

/**
 * Implementation of the Prefix span algorithm
 */
public class PrefixSpan extends GenericAlgorithm {

    /**
     * Support threshold that the k most frequent items must met
     * it is updated during the search
     */
    private int minSupport;

    /**
     * Support structure used to update the minimum support threshold during the search
     * (ordered structure)
     */
    private TreeSet < Integer > maxValuesOfK;

    public PrefixSpan ( int k ) {
        super( k );
        this.minSupport = Integer.MAX_VALUE;
        this.maxValuesOfK = new TreeSet < Integer >( Collections.reverseOrder() );
    }

    public static void main ( String[] args ) {
        if ( args.length != 3 ) {
            System.out.println( "Incorrect number of arguments! Aborting execution." );
        } else {
            String filepathPositive = args[ 0 ];
            String filepathNegative = args[ 1 ];
            int k = Integer.parseInt( args[ 2 ] );
            GenericAlgorithm genericAlgorithm = new PrefixSpan( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative, 0 ) );
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        return checkConstraints( "", pattern, patternSupportPositive, patternSupportNegative );
    }

    /**
     * {@inheritDoc}
     * This implementation keeps a list of the k higher found patterns supports and
     * update the minimum with its new lower value
     *
     */
    @Override
    void addMinElement ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        int totalSupport = getAllFoundPatterns().get( pattern ).intValue();
        if ( !maxValuesOfK.contains( totalSupport ) ) {
            super.kCounter++;
            maxValuesOfK.add( totalSupport );
            this.minSupport = maxValuesOfK.last();
        }
    }

    /**
     * {@inheritDoc}
     * This implementation prunes not frequent nodes and updates the lower bound if the new found patterns
     * was not selected before as k most frequent pattern
     */
    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Integer patternSupportPositive,
                               Integer patternSupportNegative ) {
        int totalSupport = getAllFoundPatterns().get( pattern ).intValue();
        if ( totalSupport < minSupport ) {
            return false;
        } else if ( totalSupport > minSupport && !maxValuesOfK.contains( totalSupport ) ) {
            maxValuesOfK.pollLast();
            maxValuesOfK.add( totalSupport );
            minSupport = maxValuesOfK.last();

        }
        return true;

    }

    /**
     * {@inheritDoc}
     * A node is still to be expanded if it is still frequent
     */
    @Override
    boolean isStillToBeExpanded ( Float totalSupport ) {
        return totalSupport >= minSupport;
    }

    /**
     * {@inheritDoc}
     * Here the evaluation function is just the sum of the positive and negative supports
     */
    @Override
    void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        getPositiveFoundPatterns().put( pattern, patternSupportPositive );
        getNegativeFoundPatterns().put( pattern, patternSupportNegative );
        getAllFoundPatterns().put( pattern, ( float ) ( patternSupportPositive + patternSupportNegative ) );
    }
}
