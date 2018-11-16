package SequenceMining.SupportStructures;

/**
 * This class represents a pattern that might be closed but must be checked
 * Used to contains all the useful info during the checking process
 */
public class CandidateClosedPattern {
    private int positiveSupport;
    private int negativeSupport;
    private String pattern;

    public CandidateClosedPattern ( int positiveSupport, int negativeSupport, String pattern ) {
        this.positiveSupport = positiveSupport;
        this.negativeSupport = negativeSupport;
        this.pattern = pattern;
    }

    public int getPositiveSupport () {
        return positiveSupport;
    }

    public int getNegativeSupport () {
        return negativeSupport;
    }

    public String getPattern () {
        return pattern;
    }

    public int patternLength () {
        return pattern.length();
    }
}
