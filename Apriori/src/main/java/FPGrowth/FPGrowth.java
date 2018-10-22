package FPGrowth;

import Tools.FPGrowthDataSet;

import java.util.*;
import java.util.stream.Collectors;

import static Tools.Utilities.combine;
import static Tools.Utilities.printElement;


public class FPGrowth {

    private FPTreeRoot root;
    private double minFrequency;
    private double numberOfTransactions;
    private FPTreeNode singlePathTempNode;
    private int singlePathLenght;

    public FPGrowth ( FPGrowthDataSet dataSet , double minFrequency) {
        this.root = dataSet.getRoot();
        this.minFrequency = minFrequency;
        this.numberOfTransactions = dataSet.getNumberOfTransactions();
    }

    public String starter(){
        String toPrint = "";
        if( root.getChildrens().size() == 1 ){
            if( treeContainsASinglePath(root.getChildrens().get( 0 )) ){
                int[] emptyList = {};
                return printSinglePath(emptyList);
            }
        }
        FPTreeRoot nextRoot;
        for (Integer element : getItemsInInverseOrderOfFrequence(root) ){
            if( root.getItemsSupports().get( element )/this.numberOfTransactions >= minFrequency){
                nextRoot = projectDatabase( element, root);
                int [] path = {element};
                starter(nextRoot, path);
            }

        }

        return toPrint;

    }
    private String starter ( FPTreeRoot root, int[] pattern ){
        String toPrint = "";
        if( treeContainsASinglePath(root) ){
            return printSinglePath(pattern);
        }
        FPTreeRoot newRoot;
        for (Integer element : getItemsInInverseOrderOfFrequence(root) ){
            if( root.getItemsSupports().get( element )/this.numberOfTransactions >= minFrequency){
                newRoot = projectDatabase( element , root);
                starter(newRoot, combine( pattern, element ));
            }

        }

        return toPrint;

    }

    private boolean treeContainsASinglePath( FPTreeRoot root){
        if(root.getChildrens().size() > 1) {
            return false;
        }
        singlePathLenght++;
        return treeContainsASinglePath( root.getChildrens().get( 0 ) );
    }

    private boolean treeContainsASinglePath( FPTreeNode root){
        if(root.getChildrens().size() > 1) {
            return false;
        }
        else if( root.getChildrens().size() == 0 ) {
            this.singlePathTempNode = root;
            singlePathLenght++;
            return true;
        }
        singlePathLenght++;
        return treeContainsASinglePath( root.getChildrens().get( 0 ) );
    }

    private String printSinglePath(int[] pattern){
        String toPrint = "";
        while ( singlePathTempNode != null ) {
            double frequency = singlePathTempNode.getSupport() / this.numberOfTransactions;
            if ( frequency >= minFrequency ) {
                for ( int i = 0; i < this.singlePathLenght; i++ ) {
                    toPrint = toPrint.concat( printElement( pattern, frequency,
                            singlePathTempNode, i ) );
                }
            }
            singlePathLenght--;
            singlePathTempNode = singlePathTempNode.getFather();

        }
        this.singlePathLenght=0;
        return toPrint;
    }

    private FPTreeRoot projectDatabase( int element , FPTreeRoot root){
        double support = root.getItemsSupports().get( element );
        FPTreeRoot newRoot = new FPTreeRoot();
        ArrayList<Integer> pattern;
        for(FPTreeNode node : root.getHeaderTable().get( element )){
            pattern = new ArrayList <>();
            FPTreeNode father = node.getFather();
            while( father != null){
                pattern.add( father.getKey() );
            }
            root.addTransaction( pattern, (int)support );

        }
        return newRoot;

    }
    private ArrayList<Integer> getItemsInInverseOrderOfFrequence(FPTreeRoot root){
        ArrayList<Integer> orderedChildrens =
                root.getChildrens().stream().map(FPTreeNode::getKey).collect( Collectors.toCollection(ArrayList::new));
        // sort the header table by decreasing order of support
        orderedChildrens.sort( new Comparator < Integer >() {
            public int compare ( Integer firstValue, Integer secondValue ) {
                // compare the support
                int compare =
                        ( int ) ( root.getItemsSupports().get( secondValue ) - root.getItemsSupports().get( firstValue ) );
                // if the same frequency, we check the lexical ordering!
                // otherwise we use the support
                return ( compare == 0 ) ? ( firstValue - secondValue ) : compare;
            }
        } );
        return  orderedChildrens;

    }

}
