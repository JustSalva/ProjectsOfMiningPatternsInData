package SequenceMining;

public class SupervisedSequenceMining extends GenericAlgorithm {


    public SupervisedSequenceMining ( int k ) {
        super( k );
    }

    public static void main( String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepathPositive = args[0];
            String filepathNegative = args[1];
            int k = Integer.parseInt(args[2]);
            GenericAlgorithm genericAlgorithm = new PrefixSpan( k );
            System.out.println( genericAlgorithm.start( filepathPositive, filepathNegative ,5 ) );
        }

    }

    @Override
    boolean checkConstraintsInFirstLevel ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {
        return false;
    }

    @Override
    void addMinElement ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {

    }

    @Override
    boolean checkConstraints ( String fatherPattern, String pattern, Float patternSupportPositive, Float patternSupportNegative ) {
        return false;
    }

    @Override
    void addToPatternList ( String pattern, Float patternSupportPositive, Float patternSupportNegative ) {

    }
}
