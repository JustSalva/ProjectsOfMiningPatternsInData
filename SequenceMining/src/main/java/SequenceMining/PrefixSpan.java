package SequenceMining;

import java.util.Collections;
import java.util.TreeSet;

public class PrefixSpan extends GenericAlgorithm {
    private float minSupport;
    private TreeSet<Float> maxValuesOfK;

    public PrefixSpan ( int k ) {
        super( k );
        this.minSupport = Float.MAX_VALUE;
        this.maxValuesOfK = new TreeSet<Float>( Collections.reverseOrder() ) ;
    }

    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {
        return checkConstraints( "",pattern, patternSupportPositive, patternSupportNegative );
    }

    @Override
    void addMinElement ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {
        float totalSupport = patternSupportPositive + patternSupportNegative;
        if( !maxValuesOfK.contains( totalSupport )){
            super.kCounter++;
            maxValuesOfK.add( totalSupport );
            this.minSupport = maxValuesOfK.last();
        }
    }

    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Float patternSupportPositive,
                               Float patternSupportNegative ) {
        float totalSupport = patternSupportPositive + patternSupportNegative;
        if( totalSupport < minSupport){
            return false;
        }else if(totalSupport > minSupport && !maxValuesOfK.contains( totalSupport )){
            maxValuesOfK.pollLast();
            maxValuesOfK.add( totalSupport );
            minSupport = maxValuesOfK.last();

        }
        return true;

    }

    @Override
    void addToPatternList ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {
        positiveFoundPatterns.put( pattern, patternSupportPositive);
        negativeFoundPatterns.put( pattern, patternSupportNegative );
        allFoundPatterns.put( pattern, ( patternSupportPositive+patternSupportNegative ) );
    }

    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            GenericAlgorithm genericAlgorithm = new PrefixSpan( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative , 0) );
        }

    }
}
