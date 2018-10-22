package FPGrowth;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

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
    public FPTreeNode (int key, FPTreeNode father, int support) {
        this(key,father);
        this.support = support;
    }

    public void addTransaction( ArrayList<Integer> transaction, HashMap<Integer, ArrayList<FPTreeNode>> headerTable, TreeMap<Integer, Double> itemsSupports){
        FPTreeNode currentRoot = this;
        for(Integer item : transaction){
            currentRoot = currentRoot.insertChild( item, headerTable, itemsSupports);
        }
    }

    public void addTransaction( ArrayList<Integer> transaction, HashMap<Integer, ArrayList<FPTreeNode>> headerTable,
                                int support, TreeMap<Integer, Double> itemsSupports){
        FPTreeNode currentRoot = this;
        for(Integer item : transaction){
            currentRoot = currentRoot.insertChild( item, headerTable, support, itemsSupports);
        }

    }
    public FPTreeNode insertChild( int key , HashMap<Integer, ArrayList<FPTreeNode>> headerTable, TreeMap<Integer, Double> itemsSupports){

        for(FPTreeNode children : childrens){
            if(children.getKey() == key)
                children.incrementSupport();
                addToHeaderTable(headerTable, key, children, itemsSupports, 1);
            return children;
        }
        FPTreeNode newNode = new FPTreeNode(key, this);
        headerTable.put( key, new ArrayList < FPTreeNode >(  ) );
        itemsSupports.put( key, 1.0 );
        headerTable.get( key ).add( newNode );
        childrens.add( newNode );
        addToHeaderTable(headerTable, key, newNode, itemsSupports, 1);
        return newNode; //support == 1
    }

    public FPTreeNode insertChild( int key , HashMap<Integer, ArrayList<FPTreeNode>> headerTable, int support, TreeMap<Integer, Double> itemsSupports){

        for(FPTreeNode children : childrens){
            if(children.getKey() == key)
                children.incrementSupport( support );
            addToHeaderTable( headerTable, key, children, itemsSupports, support );
            return children;
        }
        FPTreeNode newNode = new FPTreeNode(key, this);
        headerTable.put( key, new ArrayList < FPTreeNode >(  ) );
        childrens.add( newNode );
        addToHeaderTable( headerTable, key, newNode, itemsSupports, support );
        return newNode; //support == 1
    }

    private void addToHeaderTable( HashMap<Integer, ArrayList<FPTreeNode>> headerTable, int key, FPTreeNode node,
                                   TreeMap<Integer, Double> itemsSupports, int support){
        if( headerTable.containsKey( key )){
            headerTable.get( key ).add( node );
            double newSupport = itemsSupports.get( key ) + support;
            itemsSupports.replace( key, newSupport );
        }else{
            ArrayList < FPTreeNode > temp = new ArrayList < FPTreeNode >();
            temp.add( node );
            headerTable.put( key, temp );
            itemsSupports.put( key, (double)support );
        }
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
    public void incrementSupport ( int increment) {
        this.support += increment;
    }

    public FPTreeNode getFather () {
        return father;
    }
}
