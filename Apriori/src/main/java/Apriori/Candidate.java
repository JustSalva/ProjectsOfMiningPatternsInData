package Apriori;

public class Candidate {
    private int[] path;
    private int key;
    private Double support;

    public Candidate ( int[] path, int key ) {
        this.path = path;
        this.key = key;
        this.support = 0.0;
    }

    public int[] getPath () {
        return path;
    }

    public int getKey () {
        return key;
    }

    public void incrementSupport(){
        support++;
    }

    public Double getSupport () {
        return support;
    }

    public boolean isContained( int[] transaction){
        int pathIndex = 0;
        int pathLenght = this.path.length;
        for ( int transactionIndex = 0; transactionIndex < transaction.length; transactionIndex++){
            if(this.path[pathIndex]==transaction[transactionIndex]){
                pathIndex++;
            }
            if(pathIndex == pathLenght){
                return true;
            }
        }
        return false;
    }
}
