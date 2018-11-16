package SequenceMining.SupportStructures;

import java.util.HashMap;

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

    public NodeToExpand ( String pattern, HashMap < Integer, IterationState > transactionStartingPosition, Float nodeValue ) {
        this.pattern = pattern;
        this.transactionStartingPosition = transactionStartingPosition;
        this.nodeValue = nodeValue;
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

    @Override
    public int compareTo ( NodeToExpand anotherNode) {
        return Float.compare(nodeValue, anotherNode.nodeValue);
    }

}
