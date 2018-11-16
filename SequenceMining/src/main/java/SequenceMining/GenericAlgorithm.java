package SequenceMining;

import SequenceMining.DataSet.Dataset;
import SequenceMining.Exceptions.NotPresentSymbolException;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * This abstract class is the skeleton of our sequence mining application, it contains
 * the main algorithm and let children classes decide to implement how the evaluation function works,
 * which pruning rules must be applied...
 */
public abstract class GenericAlgorithm {

    /**
     * Number of k most patterns lowerBounds found so far
     */
    int kCounter;

    /**
     * k best patterns to be found
     */
    int k;

    /**
     * Map containing which patterns (key) are found in the positive database,
     *  with their support as value
     */
    private Map < String, Integer > positiveFoundPatterns;

    /**
     * Map containing which patterns (key) are found in the negative database,
     *  with their support as value
     */
    private Map < String, Integer > negativeFoundPatterns;

    /**
     * Map containing which singular items (key) are found in the both databases,
     *  with their support as value, this structure is used as an heuristic
     *  to decide which nodes to expand first, to obtain higher upper bounds faster
     */
    private LinkedHashMap < String, Integer > items;

    /**
     *  "Projected database" containing all transactions, instead of copying it at every
     *  recursive iteration we have decided to develop this structure in such a way that
     *  all accesses have complexity O(1), and save only the last visited position of each transaction
     */
    private HashMap < Integer, Transaction > transactions;

    /**
     * Map containing which patterns (key) are found in the both databases,
     *  with their support as value
     */
    private Map < String, Float > allFoundPatterns;

    /**
     *  Queue of nodes to be expanded, the heuristic and the order of exploration
     *  is to be defined in all subclasses
     */
    private PriorityQueue < NodeToExpand > nodeToExpand;

    /**
     * Initializes all the structures used by the algorithm
     * @param k the number of k best patterns to be found
     */
    public GenericAlgorithm ( int k ) {
        this.positiveFoundPatterns = new HashMap <>();
        this.negativeFoundPatterns = new HashMap <>();
        this.k = k;
        this.allFoundPatterns = new HashMap <>();
        this.kCounter = 0;
        this.nodeToExpand = new PriorityQueue <>();
        this.transactions = new HashMap <>();
    }

    /**
     * Starts the algorithm
     * @param filepathPositive file path at which is located the positive database
     * @param filePathNegative file path at which is located the negative database
     * @param numberOfDecimals number of precision requested to the evaluation function values
     * @return all the k most frequent patterns, as a string to be printed
     */
    public String start ( String filepathPositive, String filePathNegative, int numberOfDecimals ) {
        initializeDataSet( filepathPositive, filePathNegative );
        start();
        return printResults( numberOfDecimals );
    }

    /**
     * Loads both positive and negative data-sets and
     * initialize the internal structures with their content
     * @param filePathPositive file path at which is located the positive database
     * @param filePathNegative file path at which is located the negative database
     */
    protected void initializeDataSet ( String filePathPositive, String filePathNegative ) {

        Dataset dataset = new Dataset( filePathPositive, true );
        LinkedHashMap < String, Integer > positiveItems = dataset.getItems();
        ArrayList < Transaction > transactions = dataset.getTransactions();

        dataset = new Dataset( filePathNegative, false );
        transactions.addAll( dataset.getTransactions() );
        for ( int i = 0; i < transactions.size(); i++ ) {
            this.transactions.put( i, transactions.get( i ) );
        }

        joinItems( positiveItems, dataset.getItems() );
    }

    /**
     * Joins both positive and negative singular items into the items's unique structure
     * and sorts them in inverse order of frequency, to explore them later from the most
     * to the least promising one
     * @param positiveItems map containing the positive singular items
     * @param negativeItems map containing the negative singular items
     */
    protected void joinItems ( LinkedHashMap < String, Integer > positiveItems, LinkedHashMap < String, Integer > negativeItems ) {
        for ( Map.Entry < String, Integer > entry : negativeItems.entrySet() ) {
            if ( positiveItems.containsKey( entry.getKey() ) ) {
                int temp = positiveItems.get( entry.getKey() );
                positiveItems.put( entry.getKey(), temp + entry.getValue() );
            } else {
                positiveItems.put( entry.getKey(), entry.getValue() );
            }
        }
        this.items = positiveItems.entrySet().stream()
                .sorted( Collections.reverseOrder( Map.Entry.comparingByValue() ) )
                .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2 ) -> e2, LinkedHashMap::new ) );
    }

    /**
     * Starts the algorithm after the initialization process, and explore the first level of the tree,
     * in order to find the most tights bounds
     */
    private void start () {
        HashMap < Integer, IterationState > transactionStartingPosition;
        IterationState iterationState;
        int position;
        HashSet < String > newItems = new HashSet <>();
        for ( String item : items.keySet() ) {
            int patternSupportPositive = 0;
            int patternSupportNegative = 0;
            transactionStartingPosition = new HashMap <>();

            for ( int i = 0; i < transactions.size(); i++ ) {

                try {
                    position = transactions.get( i ).getPosition( 0, item, 0 );
                    iterationState = new IterationState( position );
                    iterationState.addLastTransactionIndexPerSymbol( item, position );
                    transactionStartingPosition.put( i, iterationState );
                    if ( transactions.get( i ).isPositive() ) {
                        patternSupportPositive++;
                    } else {
                        patternSupportNegative++;
                    }
                } catch ( NotPresentSymbolException e ) {
                    continue;
                }
            }
            addToPatternList( item, patternSupportPositive, patternSupportNegative );

            if ( constraintsAreMetInFirstLevel( item, patternSupportPositive, patternSupportNegative ) ) {
                nodeToExpand.add( new NodeToExpand( item, transactionStartingPosition, allFoundPatterns.get( item ) ) );
                newItems.add( item );
            }

        }
        HashSet < String > finalItems = new HashSet <>();
        for ( String item : newItems ) {
            if ( isStillToBeExpanded( allFoundPatterns.get( item ) ) ) {
                finalItems.add( item );
            }
        }

        int length = nodeToExpand.size();
        for ( int i = 0; i < length; i++ ) {
            NodeToExpand nextNode = nodeToExpand.poll();
            start( nextNode.getPattern(), nextNode.getTransactionStartingPosition(), finalItems );
        }


    }

    /**
     * Countinue the algorithm recursively after the first level's exploration,
     * the nodes are expanded from the one with better evaluation function's value
     * to the one with worst evaluation function's value (NB k most frequent patterns)
     * @param pattern pattern to be expanded recursively
     * @param transactionStartingPosition counters that are used as projected databases
     * @param items singular items that can be still expanded in the next expansion of a node
     */
    public void start ( String pattern, HashMap < Integer, IterationState > transactionStartingPosition, HashSet < String > items ) {
        HashMap < Integer, IterationState > newTransactionStartingPosition;

        IterationState iterationState;
        int position;
        boolean itemFound;
        if ( isStillToBeExpanded( allFoundPatterns.get( pattern ) ) ) {
            HashSet < String > newItems = new HashSet <>( items );
            for ( String item : items ) {
                Integer patternSupportPositive = 0;
                Integer patternSupportNegative = 0;
                newTransactionStartingPosition = new HashMap <>();
                itemFound = false;
                for ( int transactionNumber : transactionStartingPosition.keySet() ) {

                    try {
                        position =
                                transactions.get( transactionNumber ).getPosition(
                                        transactionStartingPosition.get( transactionNumber )
                                                .getLastTransactionIndexPerSymbol( item ), item, transactionStartingPosition.get( transactionNumber ).getIndexForDirectAccess() );
                        if ( position <= transactionStartingPosition.get( transactionNumber ).getIndexForDirectAccess() ) {
                            continue;
                        }
                        iterationState = new IterationState( transactionStartingPosition.get( transactionNumber ) );
                        iterationState.addLastTransactionIndexPerSymbol( item, position,
                                transactions.get( transactionNumber ).getElementMapping( item ) );
                        newTransactionStartingPosition.put( transactionNumber, iterationState );
                        if ( transactions.get( transactionNumber ).isPositive() ) {
                            patternSupportPositive++;
                        } else {
                            patternSupportNegative++;
                        }
                        itemFound = true;
                    } catch ( NotPresentSymbolException e ) {
                        continue; //unnecessary but in this way the code is more explicative
                    }
                }
                if ( !itemFound ) {
                    newItems.remove( item );
                }
                if ( patternSupportPositive > 0 || patternSupportNegative > 0 ) {
                    String temp = pattern.concat( ", " + item );
                    addToPatternList( temp, patternSupportPositive, patternSupportNegative );
                    if ( constraintsAreMet( pattern, temp, patternSupportPositive, patternSupportNegative ) ) {
                        nodeToExpand.add( new NodeToExpand( temp, newTransactionStartingPosition,
                                allFoundPatterns.get( temp ) ) );
                        if ( !nodeToExpand.isEmpty() ) {
                            NodeToExpand nextNode = nodeToExpand.poll();

                            start( nextNode.getPattern(), nextNode.getTransactionStartingPosition(),
                                    new HashSet <>( newItems ) );
                        }

                    }
                }

            }
        }
    }

    /**
     * Checks if the constraints posed are met by a new node, for the first k nodes added,
     * it just adds them into the k most frequent pattern ordered queue
     * @param fatherPattern node from which the node has been created
     * @param pattern node to be evaluated
     * @param patternSupportPositive positive support of the node
     * @param patternSupportNegative negative support of the node
     * @return true if the constraints are met, false otherwise
     */
    private boolean constraintsAreMet ( String fatherPattern, String pattern, Integer patternSupportPositive,
                                        Integer patternSupportNegative ) {
        if ( kCounter < k ) {

            addMinElement( pattern, patternSupportPositive, patternSupportNegative );
            return true;
        } else {
            return checkConstraints( fatherPattern, pattern, patternSupportPositive, patternSupportNegative );
        }
    }

    /**
     * Checks if the constraints are met in the first level of the search tree, ad-hoc method
     * needed since in the first level we cannot impose constraints related to the father node.
     * for the first k nodes added, it just adds them into the k most frequent pattern ordered queue
     * @param pattern node to be evaluated
     * @param patternSupportPositive positive support of the node
     * @param patternSupportNegative negative support of the node
     * @return true if the constraints are met, false otherwise
     */
    private boolean constraintsAreMetInFirstLevel ( String pattern, Integer patternSupportPositive,
                                                    Integer patternSupportNegative ) {
        if ( kCounter < k ) {

            addMinElement( pattern, patternSupportPositive, patternSupportNegative );
            return true;
        } else {
            return checkConstraintsInFirstLevel( pattern, patternSupportPositive, patternSupportNegative );
        }
    }

    /**
     * Actually checks it the constraints are met in the first level
     * @param pattern node to be evaluated
     * @param patternSupportPositive positive support of the node
     * @param patternSupportNegative negative support of the node
     * @return true if the constraints are met, false otherwise
     */
    abstract boolean checkConstraintsInFirstLevel ( String pattern, Integer patternSupportPositive,
                                                    Integer patternSupportNegative );

    /**
     * Adds an element to the list of k most higher evaluation function values,
     * and updates the current lower bound
     * @param pattern node to be evaluated
     * @param patternSupportPositive positive support of the node
     * @param patternSupportNegative negative support of the node
     */
    abstract void addMinElement ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative );

    /**
     * Actually checks it the constraints are met in a level that is not the first
     * @param fatherPattern node from which the node has been created
     * @param pattern node to be evaluated
     * @param patternSupportPositive positive support of the node
     * @param patternSupportNegative negative support of the node
     * @return true if the constraints are met, false otherwise
     */
    abstract boolean checkConstraints ( String fatherPattern, String pattern, Integer patternSupportPositive,
                                        Integer patternSupportNegative );

    /**
     * Checks if a pattern extracted from the priority queue is still to be expanded or
     * it has become infrequent
     * @param score score of the pattern to be evaluated
     * @return
     */
    abstract boolean isStillToBeExpanded ( Float score );

    /**
     * Adds a pattern to the list of frequent patterns
     * @param pattern node to be added
     * @param patternSupportPositive positive support of the pattern
     * @param patternSupportNegative negative support of the pattern
     */
    abstract void addToPatternList ( String pattern, Integer patternSupportPositive, Integer patternSupportNegative );


    /**
     * Extracts the actual k most frequent patterns, by ordering the found pattern's list
     * and prints them in a string
     * N.B. since the score lower bounds are updated during the search
     * some patterns may have to be eliminated
     * @param numberOfDecimals number of precision requested to the evaluation function values
     * @return all the k most frequent patterns, as a string to be printed
     */
    public String printResults ( int numberOfDecimals ) {
        String format = "%." + Integer.toString( numberOfDecimals ) + "f";
        LinkedHashMap < String, Float > result = allFoundPatterns.entrySet()
                .stream()
                .sorted( Collections.reverseOrder( Map.Entry.comparingByValue() ) )
                .collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2 ) -> e2,
                        LinkedHashMap::new ) );
        int counter = 0;
        float previousFrequency = 0;
        String toPrint = "";
        for ( Map.Entry < String, Float > entry : result.entrySet() ) {
            String key = entry.getKey();

            float nextFrequency = allFoundPatterns.get( key );
            int positiveFrequency = positiveFoundPatterns.get( key );
            int negativeFrequency = negativeFoundPatterns.get( key );

            if ( previousFrequency != nextFrequency ) {
                previousFrequency = nextFrequency;
                counter++;
            }
            if ( counter > k ) {
                break;
            } else {
                toPrint =
                        toPrint.concat( "[" + key + "]"
                                + " " + Integer.toString( positiveFrequency )
                                + " " + Integer.toString( negativeFrequency )
                                + " " + String.format( format, nextFrequency ) + "\n" );
            }
        }
        return toPrint;

    }

    public Map < String, Integer > getPositiveFoundPatterns () {
        return positiveFoundPatterns;
    }

    public void setPositiveFoundPatterns ( Map < String, Integer > positiveFoundPatterns ) {
        this.positiveFoundPatterns = positiveFoundPatterns;
    }

    public Map < String, Integer > getNegativeFoundPatterns () {
        return negativeFoundPatterns;
    }

    public void setNegativeFoundPatterns ( Map < String, Integer > negativeFoundPatterns ) {
        this.negativeFoundPatterns = negativeFoundPatterns;
    }

    public HashMap < Integer, Transaction > getTransactions () {
        return transactions;
    }

    public void setTransactions ( HashMap < Integer, Transaction > transactions ) {
        this.transactions = transactions;
    }

    public Map < String, Float > getAllFoundPatterns () {
        return allFoundPatterns;
    }

    public void setAllFoundPatterns ( Map < String, Float > allFoundPatterns ) {
        this.allFoundPatterns = allFoundPatterns;
    }


}
