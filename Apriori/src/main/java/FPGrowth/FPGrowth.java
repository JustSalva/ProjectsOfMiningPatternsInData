package FPGrowth;

import Tools.FPGrowthDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static Tools.Utilities.printElement;


public class FPGrowth {
    private TreeMap<Integer, Double> itemsFound;
    private ArrayList< FPTreeNode > childrens;
    private HashMap<Integer, ArrayList< FPTreeNode > > headerTable;
    private double minFrequency;
    private double numberOfTransactions;
    private FPTreeNode singlePathTempNode;

    public FPGrowth ( FPGrowthDataSet dataSet , double minFrequency) {
        this.itemsFound = dataSet.getItemsFound();
        this.childrens = dataSet.getRoot().getChildrens();
        this.headerTable = dataSet.getHeaderTable();
        this.minFrequency = minFrequency;
        this.numberOfTransactions = dataSet.getNumberOfTransactions();
    }

    public String starter(){
        String toPrint = "";
        /*if( treeContainsASinglePath(root) ){
            return printSinglePath(root, pattern);
        }
        for (Integer element : headerTable.keySet()){
            projectDatabase();
        }
        */
        return toPrint;

    }
    public String starter(FPTreeNode root, int[] pattern){
        String toPrint = "";
        if( treeContainsASinglePath(root) ){
            return printSinglePath(pattern);
        }
        for (Integer element : headerTable.keySet()){
            projectDatabase();
        }

        return toPrint;

    }

    private boolean treeContainsASinglePath( FPTreeNode root){
        if(root.getChildrens().size() > 1) {
            return false;
        }
        else if( root.getChildrens().size() == 0 ) {
            this.singlePathTempNode = root;
            return true;
        }
        return treeContainsASinglePath( root.getChildrens().get( 0 ) );
    }
    private String printSinglePath(int[] pattern){
        String toPrint = "";
        while (true){
            if(singlePathTempNode == null){
                break;
            }
            double frequency = singlePathTempNode.getSupport()/this.numberOfTransactions;
            if( frequency >= minFrequency)
            toPrint = toPrint.concat( printElement( pattern, frequency,
                    singlePathTempNode ));
            singlePathTempNode = singlePathTempNode.getFather();

        }
        return toPrint;
    }

    private void projectDatabase(){

    }

}
