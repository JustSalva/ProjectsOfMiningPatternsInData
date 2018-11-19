package SequenceMining;

import SequenceMining.SupportStructures.CandidateClosedPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Supervised closed sequence mining algorithm
 * that uses the absolute value of the Wracc function
 */
public class AbsoluteWracc extends SupervisedClosedSequenceMining {
    private float minN;
    private float SQUARED_N_PLUS_P_DIVIDED_BY_P;

    public AbsoluteWracc ( int k ) {
        super( k );
    }

    public static void main ( String[] args ) {
        if ( args.length != 3 ) {
            System.out.println( "Incorrect number of arguments! Aborting execution." );
        } else {
            String filepathPositive = args[ 0 ];
            String filepathNegative = args[ 1 ];
            int k = Integer.parseInt( args[ 2 ] );
            GenericAlgorithm genericAlgorithm = new AbsoluteWracc( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative, 5 ) );
        }

    }

    public static String performances ( String[] args ) {
        String filepathPositive = args[ 0 ];
        String filepathNegative = args[ 1 ];
        int k = Integer.parseInt( args[ 2 ] );
        GenericAlgorithm genericAlgorithm = new AbsoluteWracc( k );
        return genericAlgorithm.start( filepathPositive, filepathNegative, 5 );
    }

    /**
     * {@inheritDoc}
     * This implementation also initialize a constant, useful to avoid unnecessary
     * recomputations during the algorithm's elaboration
     */
    @Override
    protected void initializeDataSet ( String filePathPositive, String filePathNegative ) {
        super.initializeDataSet( filePathPositive, filePathNegative );
        this.SQUARED_N_PLUS_P_DIVIDED_BY_P = SQUARED_N_PLUS_P / ( float ) P;
    }

    /**
     * {@inheritDoc}
     * In this implementation we have an additional constant, related to the new bound:
     * the minimum value of N
     */
    @Override
    void computeConstraintConstants () {
        super.computeConstraintConstants();
        this.minN = getMinEvaluationFunction() * SQUARED_N_PLUS_P_DIVIDED_BY_P;
    }

    /**
     * {@inheritDoc}
     * In this implementation we have an additional constraint, this time on the number of
     * negative examples
     */
    @Override
    boolean lowerBoundConstraints ( int p, int n ) {
        return p >= getMinP() || n >= minN;
    }

    /**
     * {@inheritDoc}
     * In this implementation we just compute the absolute value of the Wracc function
     */
    @Override
    float computeEvaluationFunction ( Integer patternSupportPositive, Integer patternSupportNegative ) {
        return Math.abs( super.computeEvaluationFunction( patternSupportPositive, patternSupportNegative ) );
    }

    /**
     * Extracts the closed items from a list, but obviously before doing so, it separates the ones with different
     * support
     * @param candidates patterns to be filtered
     * @return the closed patterns contained in candidates
     */
    @Override
    ArrayList < CandidateClosedPattern > extractClosedItems ( ArrayList < CandidateClosedPattern > candidates ) {
        Map < String, ArrayList < CandidateClosedPattern > > patternsWithSameSupport = new HashMap <>();
        ArrayList < CandidateClosedPattern > tempList;
        for ( CandidateClosedPattern candidate : candidates ) {

            String hash = Integer.toString( candidate.getPositiveSupport() )
                    + "," + Integer.toString( candidate.getNegativeSupport() );

            if ( patternsWithSameSupport.containsKey( hash ) ) {
                tempList = patternsWithSameSupport.get( hash );
            } else {
                tempList = new ArrayList <>();
            }
            tempList.add( candidate );
            patternsWithSameSupport.put( hash, tempList );
        }
        ArrayList < CandidateClosedPattern > results = new ArrayList <>();
        for ( String key : patternsWithSameSupport.keySet() ) {
            results.addAll( super.extractClosedItems( patternsWithSameSupport.get( key ) ) );
        }
        return results;

    }
}
