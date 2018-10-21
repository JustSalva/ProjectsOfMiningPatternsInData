package Apriori;

import java.util.Set;
import java.util.TreeMap;

/**
 * Implementation of the HashTree data structure, to contain the Apriori search tree
 */
public class HashTree {
    /**
     * HashMap containing the children nodes of this node
     */
    private TreeMap<Integer, HashTree> treeMap;
    /**
     * frequency of the node
     */
    private double frequency;


    HashTree () {
        this.treeMap = new TreeMap <>();
        this.frequency = 0;
    }

    private HashTree ( double frequency ) {
        this.treeMap = new TreeMap <>();
        this.frequency = frequency;
    }


    /**
     * Adds an element as a children
     * @param value key of the children
     * @param frequency frequency of the new node
     */
    void addElement ( int value, double frequency ){
        if( ! treeMap.containsKey( value ) ){
            treeMap.put( value , new HashTree(frequency));
        }
        else{
            System.out.println( "WARNING - double attempt to create same node" );
        }
    }

    TreeMap < Integer, HashTree > getHashTree () {
        return treeMap;
    }

    Set<Integer> getKeys (){
        return treeMap.keySet();
    }

    public Double getFrequency () {
        return frequency;
    }

    public void incrementFrequency () {
        this.frequency += 1;
    }


}
