package SequenceMining;

import SequenceMining.DataSet.Dataset;

import java.util.*;

public class SupervisedSequenceMining extends GenericAlgorithm{
    int P;
    private int N;
    private float WEIGHT;
    private TreeSet<Float> maxValuesOfK;
    float minWracc;
    float minP;
    float SQUARED_N_PLUS_P;
    private float SQUARED_N_PLUS_P_DIVIDED_BY_N;

    public SupervisedSequenceMining ( int k ) {
        super( k );
        this.maxValuesOfK = new TreeSet <>( Collections.reverseOrder());
    }

    @Override
    protected void initializeDataSet ( String filePathPositive, String filePathNegative){

        Dataset dataset = new Dataset( filePathPositive, true );
        LinkedHashMap<String, Integer> positiveItems = dataset.getItems();
        ArrayList<Transaction> transactions = dataset.getTransactions();
        this.P = transactions.size();
        dataset = new Dataset( filePathNegative , false );
        transactions.addAll( dataset.getTransactions() );
        for( int i=0; i < transactions.size(); i++ ){
            this.getTransactions().put( i, transactions.get( i ) );
        }
        this.N = transactions.size() - P;
        this.WEIGHT = ( ( (float)P/(float)(N+P) ) * (float)N/(float)(N+P) );
        this.SQUARED_N_PLUS_P = (N+P)*(N+P);
        this.SQUARED_N_PLUS_P_DIVIDED_BY_N = SQUARED_N_PLUS_P / (float)N;


        joinItems( positiveItems, dataset.getItems() );
    }

    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, pattern);
    }

    @Override
    void addMinElement ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {

        float totalSupport = getAllFoundPatterns().get( pattern );
        if( !maxValuesOfK.contains( totalSupport )){
            super.kCounter++;
            maxValuesOfK.add( totalSupport );
            this.minWracc = maxValuesOfK.last();
            computeConstraintConstants();
        }
    }

    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        boolean respectsLowerBounds = lowerBoundConstraints( patternSupportPositive, patternSupportNegative );
        return checkConstraints( respectsLowerBounds, pattern);

    }

    @Override
    boolean isStillToBeExpanded ( Float score ) {
        return true;
    }

    boolean checkConstraints ( boolean respectsLowerBounds, String pattern) {
        float wracc = getAllFoundPatterns().get( pattern );
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
    void computeConstraintConstants(){
        this.minP = minWracc * SQUARED_N_PLUS_P_DIVIDED_BY_N;
    }
    boolean lowerBoundConstraints(int p, int n){
        //return p >= minP || n >= minN;
        return p >= minP;
    }


    @Override
    void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative ) {
        getPositiveFoundPatterns().put( pattern, patternSupportPositive);
        getNegativeFoundPatterns().put( pattern, patternSupportNegative );
        getAllFoundPatterns().put( pattern, computeEvaluationFunction(patternSupportPositive, patternSupportNegative));
    }

    float computeEvaluationFunction( Integer patternSupportPositive, Integer patternSupportNegative){
        return (float)(Math.round(WEIGHT * ( ((float)patternSupportPositive/(float)P) - ((float)patternSupportNegative/(float)N) ) * 100000d) / 100000d);

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
