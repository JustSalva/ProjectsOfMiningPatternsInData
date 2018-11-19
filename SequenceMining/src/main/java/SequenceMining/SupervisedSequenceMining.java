package SequenceMining;

import SequenceMining.DataSet.Dataset;
import SequenceMining.SupportStructures.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.TreeSet;

/**
 * Implementation of the supervised sequence mining algorithm,
 * with a Wracc evaluation function
 */
public class SupervisedSequenceMining extends GenericAlgorithm {

    /**
     * Number of positive transactions
     */
    int P;

    /**
     * Number of negative transactions
     */
    int N;
    /**
     * = N^2 + P , constant used to avoid repeating useless recalculations
     */
    float SQUARED_N_PLUS_P;
    /**
     * = (N^2 + P ) / N , constant used to avoid repeating useless recalculations
     */
    float SQUARED_N_PLUS_P_DIVIDED_BY_N;
    /**
     * Constant weight of the Wracc function =  ( P / ( N + P ) ) * ( N / ( N + P ) )
     */
    private float WEIGHT;
    /**
     * Support structure used to update the minimum values of the Wracc during the search
     * (ordered structure)
     */
    private TreeSet < Float > maxValuesOfK;
    /**
     * Wracc threshold that the k most frequent items must met
     * it is updated during the search
     */
    private float minEvaluationFunction;
    /**
     * Minimum value of the positive items, to be used as a lower bound
     */
    private float minP;


    public SupervisedSequenceMining ( int k ) {
        super( k );
        this.maxValuesOfK = new TreeSet <>( Collections.reverseOrder() );
    }

    public static void main ( String[] args ) {
        if ( args.length != 3 ) {
            System.out.println( "Incorrect number of arguments! Aborting execution." );
        } else {
            String filepathPositive = args[ 0 ];
            String filepathNegative = args[ 1 ];
            int k = Integer.parseInt( args[ 2 ] );
            GenericAlgorithm genericAlgorithm = new SupervisedSequenceMining( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative, 5 ) );
        }

    }

    public static String performances ( String[] args ) {
        String filepathPositive = args[ 0 ];
        String filepathNegative = args[ 1 ];
        int k = Integer.parseInt( args[ 2 ] );
        GenericAlgorithm genericAlgorithm = new SupervisedSequenceMining( k );
        return genericAlgorithm.start( filepathPositive, filepathNegative, 5 );
    }

    /**
     * {@inheritDoc}
     * This implementation also initialize some constants to be used to avoid recalculations
     * and counts the P and N cardinalities
     */
    @Override
    protected void initializeDataSet ( String filePathPositive, String filePathNegative ) {

        Dataset dataset = new Dataset( filePathPositive, true );
        LinkedHashMap < String, Integer > positiveItems = dataset.getItems();
        ArrayList < Transaction > transactions = dataset.getTransactions();
        this.P = transactions.size();
        dataset = new Dataset( filePathNegative, false );
        transactions.addAll( dataset.getTransactions() );
        for ( int i = 0; i < transactions.size(); i++ ) {
            this.getTransactions().put( i, transactions.get( i ) );
        }
        this.N = transactions.size() - P;
        this.WEIGHT = ( ( ( float ) P / ( float ) ( N + P ) ) * ( float ) N / ( float ) ( N + P ) );
        this.SQUARED_N_PLUS_P = ( N + P ) * ( N + P );
        this.SQUARED_N_PLUS_P_DIVIDED_BY_N = SQUARED_N_PLUS_P / ( float ) N;


        joinItems( positiveItems, dataset.getItems() );
    }

    /**
     * {@inheritDoc}
     * This implementation can only checks if a lower bound on p is met
     */
    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, pattern );
    }

    /**
     * {@inheritDoc}
     * This implementation keeps a list of the k higher found Wracc values and
     * update the minimum with its new lower value
     */
    @Override
    void addMinElement ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {

        float totalSupport = getAllFoundPatterns().get( pattern );
        if ( !maxValuesOfK.contains( totalSupport ) ) {
            super.kCounter++;
            maxValuesOfK.add( totalSupport );
            this.minEvaluationFunction = maxValuesOfK.last();
            computeConstraintConstants();
        }
    }

    /**
     * {@inheritDoc}
     * This implementation can only checks if a lower bound on p is met
     */
    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, pattern );

    }

    /**
     * {@inheritDoc}
     * This implementation cannot prune nodes when they are extracted from the nodes to be expanded
     * queue since the Wracc is not anti-monotonic
     */
    @Override
    boolean isStillToBeExpanded ( Float score ) {
        return true;
    }

    /**
     * {@inheritDoc}
     * Here the evaluation function is the Wracc function
     */
    @Override
    void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        getPositiveFoundPatterns().put( pattern, patternSupportPositive );
        getNegativeFoundPatterns().put( pattern, patternSupportNegative );
        getAllFoundPatterns().put( pattern, computeEvaluationFunction( patternSupportPositive, patternSupportNegative ) );
    }


    /**
     * Checks if the constraints are met and updates the k most higher values of Wracc's
     * list and the minimum Wracc value, if needed
     *
     * @param respectsLowerBounds true if the pattern respects the lower bound
     * @param pattern             pattern to be evaluated
     * @return true if the node can be expanded, false otherwise
     */
    private boolean checkConstraints ( boolean respectsLowerBounds, String pattern ) {
        float wracc = getAllFoundPatterns().get( pattern );
        if ( !( respectsLowerBounds ) ) {
            return false;
        } else if ( wracc > minEvaluationFunction && !maxValuesOfK.contains( wracc ) ) {
            maxValuesOfK.pollLast();
            maxValuesOfK.add( wracc );
            minEvaluationFunction = maxValuesOfK.last();
            computeConstraintConstants();

        }
        return true;
    }

    /**
     * Updates the constants used in the bounds after an update of the min Wracc value
     */
    void computeConstraintConstants () {
        this.minP = minEvaluationFunction * SQUARED_N_PLUS_P_DIVIDED_BY_N;
    }

    /**
     * Checks if the lower bound constraints are met
     *
     * @param p positive support of the pattern to be evaluated
     * @param n negative support of the pattern to be evaluated
     * @return true if they are met, false otherwise
     */
    boolean lowerBoundConstraints ( int p, int n ) {
        return p >= minP;
    }

    /**
     * Computes the Wracc evaluation function
     *
     * @param patternSupportPositive positive support of the pattern to be evaluated
     * @param patternSupportNegative negative support of the pattern to be evaluated
     * @return the computed value
     */
    float computeEvaluationFunction ( Integer patternSupportPositive, Integer patternSupportNegative ) {
        return ( float ) ( Math.round( WEIGHT * ( ( ( float ) patternSupportPositive / ( float ) P ) - ( ( float ) patternSupportNegative / ( float ) N ) ) * 100000d ) / 100000d );

    }

    public float getMinEvaluationFunction () {
        return minEvaluationFunction;
    }

    public float getMinP () {
        return minP;
    }

}
