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
        createFirstLevel();
        int [] path = {};
        return starter( root, path);

    }

    public String starter ( HashTree father, int[] path){
        String toReturn = "";
        Set<Integer> keys = father.getKeys();
        ArrayList<Integer> keysToExpand = new ArrayList<>(keys);
        for ( Integer key : keys){
            keysToExpand.remove( key );
            ArrayList<Candidate> candidates = generateCandidates( father, key, keysToExpand, combine( path, key ));
            computeSupport(candidates, key, father);
            for( Map.Entry<Integer, HashTree> elected: father.getHashTree().entrySet()){
                toReturn = toReturn.concat( starter( elected.getValue(), combine( path, elected.getKey() )) );
            }
        }
        return toReturn.concat( father.toString( path ));

    }

    private ArrayList<Candidate> generateCandidates(HashTree father, Integer key, ArrayList<Integer> keyToExpand,
                                                int[] path){
        ArrayList<Candidate> candidates = new ArrayList <>();
        for(int nextKey : keyToExpand){
            candidates.add( new Candidate(combine(path, nextKey), key ));
        }
        return candidates;
    }

    private void computeSupport (ArrayList<Candidate> candidates , Integer key, HashTree father){
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
            }
        }
    }

    private void createFirstLevel(){
        double frequency;
        // First level of the tree
        for ( Map.Entry<Integer, Double> entry : dataset.getItems().entrySet()){
            frequency = entry.getValue() / transactionNumber;
            if( frequency >= minFrequency){
                root.addElement(entry.getKey(),frequency);
            }

        }
    }

    private static int[] combine(int[] list, int element) {
        int length = list.length + 1;
        int[] result = new int[ length ];
        System.arraycopy( list, 0, result, 0, list.length );
        result[result.length-1] = element;
        return result;
    }

}
