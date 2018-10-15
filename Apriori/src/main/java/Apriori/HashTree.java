package Apriori;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class HashTree {
    private TreeMap<Integer, HashTree> treeMap;
    private double frequency;


    public HashTree () {
        this.treeMap = new TreeMap <>();
        this.frequency = 0;
    }

    public HashTree ( double frequency ) {
        this.treeMap = new TreeMap <>();
        this.frequency = frequency;
    }

    public TreeMap < Integer, HashTree > getHashTree () {
        return treeMap;
    }

    public void addElement( int value, double frequency){
        if( ! treeMap.containsKey( value ) ){
            treeMap.put( value , new HashTree(frequency));
        }
        else{
            System.out.println( "WARNING - double attempt to create same node" );
        }
    }

    public Set<Integer> getKeys (){
        return treeMap.keySet();
    }

    public Double getFrequency () {
        return frequency;
    }

    public void incrementFrequency () {
        this.frequency += 1;
    }

    public String toString ( int[] path) {
        String toPrint = "";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() );
        }
        return toPrint.concat( "("+String.format("%.2f", frequency) +")" + "\n");
    }

}
