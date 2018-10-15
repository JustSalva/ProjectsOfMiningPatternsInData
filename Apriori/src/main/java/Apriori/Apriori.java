package Apriori;

import Tools.Dataset;

import java.util.*;

public class Apriori {
    private Dataset dataset;
    private double minFrequency;
    private int transactionNumber;
    private HashTree root;

    public Apriori ( Dataset dataset, double minFrequency ) {
        this.dataset = dataset;
        this.minFrequency = minFrequency;
        this.transactionNumber = dataset.transactionNumber();
        this.root = new HashTree();
    }
    public String starter(){
        String toPrint = createFirstLevel();
        int [] path = {};
        toPrint = toPrint.concat( starter( root, path) ) ;
        return toPrint;

    }

    public String starter ( HashTree father, int[] path){
        String toReturn = "";
        Set<Integer> keys = father.getKeys();
        ArrayList<Integer> keysToExpand = new ArrayList<>(keys);
        for ( Integer key : keys){
            keysToExpand.remove( key );
            ArrayList<Candidate> candidates = generateCandidates( father, key, keysToExpand, combine( path, key ));
            toReturn = toReturn.concat( computeSupport(candidates, key, father.getHashTree().get( key ), path) );
        }
        if(father.getHashTree().entrySet().size() > 1 ) {
            for ( Map.Entry < Integer, HashTree > elected : father.getHashTree().entrySet() ){
                toReturn = toReturn.concat( starter( elected.getValue(), combine( path, elected.getKey() ) ) );
            }
        }
        return toReturn;

    }

    private ArrayList<Candidate> generateCandidates(HashTree father, Integer key, ArrayList<Integer> keyToExpand,
                                                int[] path){
        ArrayList<Candidate> candidates = new ArrayList <>();
        for(int nextKey : keyToExpand){
            candidates.add( new Candidate(combine(path, nextKey), nextKey ));
        }
        return candidates;
    }

    private String computeSupport (ArrayList<Candidate> candidates , Integer key, HashTree father, int[] path){
        String toPrint = "";
        for( int[] transaction :dataset.transactions()){
            for( Candidate candidate: candidates ){
                if( candidate.isContained( transaction )){
                    candidate.incrementSupport();
                }
            }
        }
        for( Candidate candidate: candidates ){
            double frequency  = candidate.getSupport() / this.transactionNumber;
            if( frequency >= this.minFrequency ){
                father.addElement( candidate.getKey(), frequency);
                toPrint = toPrint.concat( printElement( path, key, candidate.getKey(), frequency ) );
            }
        }
        return toPrint;
    }

    private String createFirstLevel(){
        double frequency;
        String toPrint = "";
        int [] emptyList = {};
        // First level of the tree
        for ( Map.Entry<Integer, Double> entry : dataset.getItems().entrySet()){
            frequency = entry.getValue() / transactionNumber;
            if( frequency >= minFrequency){
                root.addElement(entry.getKey(),frequency);
                toPrint = toPrint.concat( printElement(entry.getKey(), frequency ) );
            }

        }
        return toPrint;
    }

    private static int[] combine(int[] list, int element) {
        int length = list.length + 1;
        int[] result = new int[ length ];
        System.arraycopy( list, 0, result, 0, list.length );
        result[result.length-1] = element;
        return result;
    }
    private String printElement(int[] path, int fatherKey, int candidateKey, double frequency){
        String toPrint = "[";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        toPrint = toPrint.concat( fatherKey + ", "+candidateKey+"]" );
        return toPrint.concat( "  ("+String.format("%.3f", frequency) +")" + "\n");
    }

    private String printElement(int candidateKey, double frequency){
        String toPrint = "["+ candidateKey +"]" ;
        return toPrint.concat( "  ("+String.format("%.3f", frequency) +")" + "\n");
    }

}
