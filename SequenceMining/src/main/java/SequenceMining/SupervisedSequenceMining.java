package SequenceMining;

import SequenceMining.DataSet.Dataset;

import java.util.*;

public class SupervisedSequenceMining extends GenericAlgorithm{
    private int P;
    private int N;
    private float WEIGHT;
    private TreeSet<Float> maxValuesOfK;
    private float minWracc;
    private float minP;
    private float SQUARED_N_PLUS_P;
    private float SQUARED_N_PLUS_P_DIVIDED_BY_N;
    private float INVERSE_P;
    private float minWracc_times_SQUARED_N_PLUS_P;
    //private float minN;

    public SupervisedSequenceMining ( int k ) {
        super( k );
        this.maxValuesOfK = new TreeSet <>( Collections.reverseOrder());
    }

    @Override
    protected void initializeDataset(String filePathPositive, String filePathNegative){

        Dataset dataset = new Dataset( filePathPositive, true );
        LinkedHashMap<String, Integer> positiveItems = dataset.getItems();
        ArrayList<Transaction> transactions = dataset.getTransactions();
        this.P = transactions.size();
        dataset = new Dataset( filePathNegative , false );
        transactions.addAll( dataset.getTransactions() );
        for( int i=0; i < transactions.size(); i++ ){
            this.transactions.put( i, transactions.get( i ) );
        }
        this.N = transactions.size() - P;
        this.WEIGHT = ( ( (float)P/(float)(N+P) ) * (float)N/(float)(N+P) );
        this.SQUARED_N_PLUS_P = (N+P)*(N+P);
        this.INVERSE_P = 1.0f/(float)P;
        this.SQUARED_N_PLUS_P_DIVIDED_BY_N = SQUARED_N_PLUS_P / (float)N;
        joinItems( positiveItems, dataset.getItems() );
    }

    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        boolean respectsUpperBounds = upperBoundConstraints( P,N,patternSupportPositive, patternSupportNegative );
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, respectsUpperBounds, pattern);
    }

    @Override
    void addMinElement ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {

        float totalSupport = allFoundPatterns.get( pattern );
        if( !maxValuesOfK.contains( totalSupport )){
            super.kCounter++;
            maxValuesOfK.add( totalSupport );
            this.minWracc = maxValuesOfK.last();
            computeConstraintConstants();
        }
    }

    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {

        boolean respectsUpperBounds = upperBoundConstraints( positiveFoundPatterns.get( fatherPattern ),
                                                                negativeFoundPatterns.get( fatherPattern ),
                                                                patternSupportPositive,
                                                                patternSupportNegative );
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, respectsUpperBounds, pattern);

    }

    @Override
    boolean isStillToBeExpanded ( Float score ) {
        return true;
    }

    boolean checkConstraints ( boolean respectsLowerBounds, boolean respectsUpperBounds, String pattern) {
        float wracc = allFoundPatterns.get( pattern );
        if( !(respectsLowerBounds )){
            return false;
        }else if(wracc > minWracc && !maxValuesOfK.contains( wracc )){
            maxValuesOfK.pollLast();
            maxValuesOfK.add( wracc );
            minWracc = maxValuesOfK.last();
            computeConstraintConstants();

        }
        return true;
    }


        private void computeConstraintConstants(){
        this.minP = minWracc * SQUARED_N_PLUS_P_DIVIDED_BY_N;
        this.minWracc_times_SQUARED_N_PLUS_P = SQUARED_N_PLUS_P* minWracc;
        //this.minN = minWracc * N;
    }

    private boolean upperBoundConstraints(int nFather, int pFather, int p, int n){
       // return ( n <= ( (N * (pFather - minP) ) / P ) ) || ( p <= ( (P * (nFather - minN) ) / N ) );
        return n <= ( INVERSE_P * (((float)N * (float)pFather) - minWracc_times_SQUARED_N_PLUS_P) );
    }
    private boolean lowerBoundConstraints(int p, int n){
        //return p >= minP || n >= minN;
        return p >= minP;
    }


    @Override
    void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        positiveFoundPatterns.put( pattern, patternSupportPositive);
        negativeFoundPatterns.put( pattern, patternSupportNegative );
        allFoundPatterns.put( pattern,
                (float)(Math.round(WEIGHT * ( ((float)patternSupportPositive/(float)P) - ((float)patternSupportNegative/(float)N) ) * 100000d) / 100000d)

        );
    }

    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            GenericAlgorithm genericAlgorithm = new SupervisedSequenceMining( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative ,5 ) );
        }

    }


}
