package Apriori;

/**
 * Support class that represent a candidate node (= node that is potentially to be expanded)
 */
public class Candidate {

    /**
     * path that leads to this node
     */
    private int[] path;

    /**
     * key value of this node
     */
    private int key;

    /**
     *  number of occurrences of this pattern
     */
    private Double support;

    Candidate ( int[] path, int key ) {
        this.path = path;
        this.key = key;
        this.support = 0.0;
    }

    public int[] getPath () {
        return path;
    }

    int getKey () {
        return key;
    }

    void incrementSupport (){
        support++;
    }

    Double getSupport () {
        return support;
    }

    boolean isContained ( int[] transaction ){
        int pathIndex = 0;
        int pathLenght = this.path.length;
        for ( int aTransaction : transaction ) {
            if ( this.path[ pathIndex ] == aTransaction ) {
                pathIndex++;
            }
            if ( pathIndex == pathLenght ) {
                return true;
            }
        }
        return false;
    }
}
