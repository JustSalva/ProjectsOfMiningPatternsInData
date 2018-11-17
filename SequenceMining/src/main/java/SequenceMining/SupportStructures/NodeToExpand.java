package SequenceMining.SupportStructures;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class describe a pattern's node to be expanded, when it is contained in
 * the priority queue of nodes to be expanded later
 */
public class NodeToExpand implements Comparable<NodeToExpand>{

    private String pattern;

    /**
     * Projected database of the node
     */
    private HashMap<Integer, IterationState> transactionStartingPosition;

    /**
     * Evaluation function's value of the pattern
     */
    private Float nodeValue;

    /**
     * Singular items that are still to be expanded from this node ( =frequent ones)
     */
    private HashSet< String > items;

    public NodeToExpand ( String pattern, HashMap < Integer, IterationState > transactionStartingPosition,
                          Float nodeValue, HashSet < String > items ) {
        this.pattern = pattern;
        this.transactionStartingPosition = transactionStartingPosition;
        this.nodeValue = nodeValue;
        this.items = items;
    }

    public String getPattern () {
        return pattern;
    }

    public HashMap < Integer, IterationState > getTransactionStartingPosition () {
        return transactionStartingPosition;
    }

    public Float getNodeValue () {
        return nodeValue;
    }

    public HashSet < String > getItems () {
        return items;
    }

    @Override
    public int compareTo ( NodeToExpand anotherNode) {
        return Float.compare(nodeValue, anotherNode.nodeValue);
    }

}
