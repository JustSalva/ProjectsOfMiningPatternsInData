package FPGrowth;


import java.util.ArrayList;
import java.util.HashMap;

public class FPTreeNode {
    private ArrayList< FPTreeNode > childrens;
    private int support;
    private int key;
    private FPTreeNode father;

    /**
     * Constructor for root
     */
    public FPTreeNode (int key, FPTreeNode father) {
        this.childrens = new ArrayList < FPTreeNode >();
        this.support = 1;
        this.key = key;
        this.father = father;
    }

    public void addTransaction( ArrayList<Integer> transaction, HashMap<Integer, ArrayList<FPTreeNode>> headerTable){
        FPTreeNode currentRoot = this;
        for(Integer item : transaction){
            currentRoot = currentRoot.insertChild( item, headerTable);
        }
    }
    public FPTreeNode insertChild( int key , HashMap<Integer, ArrayList<FPTreeNode>> headerTable){

        for(FPTreeNode children : childrens){
            if(children.getKey() == key)
                children.incrementSupport();
            return children;
        }
        FPTreeNode newNode = new FPTreeNode(key, this);
        headerTable.get( key ).add( newNode );
        return newNode; //support == 1
    }

    public ArrayList < FPTreeNode > getChildrens () {
        return childrens;
    }

    public int getSupport () {
        return support;
    }

    public int getKey () {
        return key;
    }

    public void incrementSupport () {
        this.support ++;
    }

    public FPTreeNode getFather () {
        return father;
    }
}
