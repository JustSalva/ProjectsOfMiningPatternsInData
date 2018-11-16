package SequenceMining;

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

    public int patternLength(){
        return pattern.length();
    }
}
