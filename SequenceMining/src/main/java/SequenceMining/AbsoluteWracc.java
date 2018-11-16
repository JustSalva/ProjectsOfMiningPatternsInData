package SequenceMining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbsoluteWracc extends SupervisedClosedSequenceMining {
    private float minN;
    private float SQUARED_N_PLUS_P_DIVIDED_BY_P;
    public AbsoluteWracc ( int k ) {
        super( k );
    }

    @Override
    protected void initializeDataSet ( String filePathPositive, String filePathNegative ) {
        super.initializeDataSet( filePathPositive, filePathNegative );
        this.SQUARED_N_PLUS_P_DIVIDED_BY_P = SQUARED_N_PLUS_P / (float)P;
    }

    @Override
    boolean lowerBoundConstraints ( int p, int n ) {
        return p >= minP || n >= minN;
    }
    @Override
    void computeConstraintConstants(){
        super.computeConstraintConstants();
        this.minN = minWracc * SQUARED_N_PLUS_P_DIVIDED_BY_P;
    }

    @Override
    float computeEvaluationFunction ( Integer patternSupportPositive, Integer patternSupportNegative ) {
        return Math.abs( super.computeEvaluationFunction( patternSupportPositive, patternSupportNegative ));
    }

    @Override
    ArrayList< CandidateClosedPattern > extractClosedItems ( ArrayList < CandidateClosedPattern > candidates ) {
        Map<String, ArrayList<CandidateClosedPattern> > patternsWithSameSupport = new HashMap <>();
        ArrayList<CandidateClosedPattern> tempList;
        for(CandidateClosedPattern candidate: candidates){

            String hash = Integer.toString( candidate.getPositiveSupport())
                            + ","+ Integer.toString( candidate.getNegativeSupport());

            if(patternsWithSameSupport.containsKey( hash )){
                tempList = patternsWithSameSupport.get( hash );
            }else{
                tempList = new ArrayList <>();
            }
            tempList.add( candidate );
            patternsWithSameSupport.put( hash, tempList );
        }
        ArrayList< CandidateClosedPattern > results = new ArrayList <>();
        for(String key: patternsWithSameSupport.keySet()){
            results.addAll( super.extractClosedItems( patternsWithSameSupport.get( key ) ) );
        }
        return results;

    }

    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            GenericAlgorithm genericAlgorithm = new AbsoluteWracc( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative ,5 ) );
        }

    }
}
