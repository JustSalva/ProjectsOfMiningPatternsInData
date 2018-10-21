package Apriori;

import Tools.AprioriDataSet;

import java.util.*;

import static Tools.Utilities.*;


/**
 * Apriori implementation
 */
public class Apriori {

    /**
     *  DataSet to be analyzed by the search
     */
    private AprioriDataSet dataset;

    /**
     * minimum frequency of the pattern to be found
     */
    private double minFrequency;

    /**
     * Total number of transactions in the database
     */
    private int numberOfTransactions;

    /**
     *  root of the search tree
     */
    private HashTree root;

    /**
     * Constructor of the class
     * @param dataset dataset to be analyzed by the algorithm
     * @param minFrequency minFrequency of the patterns that the algorithms must search for
     */
    public Apriori ( AprioriDataSet dataset, double minFrequency ) {
        this.dataset = dataset;
        this.minFrequency = minFrequency;
        this.numberOfTransactions = dataset.transactionNumber();
        this.root = new HashTree();
    }

    /**
     * Starts the search of the patterns in the database, basically it creates the first level of the tree and starts
     * the recursive chain
     * @return the patterns founded, printed as a string
     */
    public String starter(){
        String toPrint = createFirstLevel();
        int [] path = {};
        toPrint = toPrint.concat( starter( root, path) ) ;
        return toPrint;

    }

    /**
     * This is the core of our application, the main Apriori logic is here, this method first generate the
     * candidates, in a lexicographic order, merging itemsets form the upper level, it computes their support and
     * then recursively expand them.
     * @param father node form which the method must expand the search tree
     * @param path path of nodes that had lead the search to the father node
     * @return the patterns founded, printed as a string
     */
    private String starter ( HashTree father, int[] path){
        String toReturn = "";
        Set<Integer> keys = father.getKeys();
        ArrayList<Integer> keysToExpand = new ArrayList<>(keys);
        for ( Integer key : keys){
            keysToExpand.remove( key );
            ArrayList<Candidate> candidates = generateCandidates(keysToExpand, combine( path, key ));
            toReturn = toReturn.concat( computeSupport(candidates, key, father.getHashTree().get( key ), path) );
        }
        if(father.getHashTree().entrySet().size() > 1 ) {
            for ( Map.Entry < Integer, HashTree > elected : father.getHashTree().entrySet() ){
                toReturn = toReturn.concat( starter( elected.getValue(), combine( path, elected.getKey() ) ) );
            }
        }
        return toReturn;

    }

    /**
     * Generate the candidate nodes from a node (key), by using the merging technique, it takes the nodes with same
     * father node and use them as possible new leaves
     * @param keyToExpand list of nodes to be expanded
     * @param path path that has lead the search to the current level
     * @return the list of nodes that could be expanded
     */
    private ArrayList<Candidate> generateCandidates(ArrayList<Integer> keyToExpand,
                                                int[] path){
        ArrayList<Candidate> candidates = new ArrayList <>();
        for(int nextKey : keyToExpand){
            candidates.add( new Candidate(combine(path, nextKey), nextKey ));
        }
        return candidates;
    }

    /**
     * Computes the support of each element in the list and discard those element whose frequency
     * is lower than the threshold
     * @param candidates candidates nodes to be checked
     * @param key node who is the father of the candidates
     * @param father father of the node form which we've expanded the candidates
     * @param path sequence of nodes that has lead the search to the current nodes
     * @return a list of admissible patterns, printed into a string
     */
    private String computeSupport (ArrayList<Candidate> candidates , Integer key, HashTree father, int[] path){

        for( int[] transaction :dataset.transactions()){
            for( Candidate candidate: candidates ){
                if( candidate.isContained( transaction )){
                    candidate.incrementSupport();
                }
            }
        }
        return printElectedCandidates(candidates, key, father, path);
    }

    /**
     *  Given the elected candidates prints a list of admissible patterns, into a string
     * @param candidates candidates nodes to be checked
     * @param key node who is the father of the candidates
     * @param father father of the node form which we've expanded the candidates
     * @param path sequence of nodes that has lead the search to the current nodes
     * @return a list of admissible patterns, printed into a string
     */
    private String printElectedCandidates(ArrayList<Candidate> candidates , Integer key, HashTree father, int[] path){

        String toPrint = "";
        for( Candidate candidate: candidates ){
            double frequency  = candidate.getSupport() / this.numberOfTransactions;
            if( frequency >= this.minFrequency ){
                father.addElement( candidate.getKey(), frequency);
                toPrint = toPrint.concat( printElement( path, key, candidate.getKey(), frequency ) );
            }
        }
        return toPrint;
    }

    /**
     * Create the first level of a the search tree
     * @return the admissible patterns of the first level in form of a string
     */
    private String createFirstLevel(){
        double frequency;
        String toPrint = "";
        // First level of the tree
        for ( Map.Entry<Integer, Double> entry : dataset.getItems().entrySet()){
            frequency = entry.getValue() / numberOfTransactions;
            if( frequency >= minFrequency){
                root.addElement(entry.getKey(),frequency);
                toPrint = toPrint.concat( printElement(entry.getKey(), frequency ) );
            }

        }
        return toPrint;
    }




}
