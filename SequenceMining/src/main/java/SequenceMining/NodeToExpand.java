package SequenceMining;

import java.util.HashMap;
import java.util.Objects;

public class NodeToExpand implements Comparable<NodeToExpand>{

    private String pattern;
    private HashMap<Integer, IterationState> transactionStartingPosition;
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
