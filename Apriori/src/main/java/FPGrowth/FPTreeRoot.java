package FPGrowth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class FPTreeRoot {
    private ArrayList< FPTreeNode > childrens;
    private HashMap<Integer, ArrayList< FPTreeNode > > headerTable;
    private TreeMap<Integer, Double> itemsSupports;

    public FPTreeRoot () {
        this.childrens = new ArrayList < FPTreeNode >();
        this.headerTable = new HashMap <>();
        this.itemsSupports = new TreeMap <>();
    }

    public void addTransaction( ArrayList<Integer> transaction, HashMap<Integer, ArrayList<FPTreeNode>> headerTable){
        boolean found = false;
        for(FPTreeNode children : childrens){
            if(children.getKey() == transaction.get( 0 ))
                children.incrementSupport();
                double newSupport = itemsSupports.get( children.getKey() ) + 1;
                itemsSupports.replace( children.getKey(), newSupport );
                transaction.remove( 0 );
                children.addTransaction( transaction, headerTable, itemsSupports );
                found = true;
        }
        if( !found ){
            FPTreeNode newNode = new FPTreeNode(transaction.get( 0 ), null);
            headerTable.put( transaction.get( 0 ), new ArrayList < FPTreeNode >(  ) );
            headerTable.get( transaction.get( 0 ) ).add( newNode );
            itemsSupports.put( transaction.get( 0 ), 1.0 );
            childrens.add( newNode );
            transaction.remove( 0 );
            newNode.addTransaction( transaction, headerTable, itemsSupports );
        }

    }

    public void addTransaction( ArrayList<Integer> transaction, int support){
        boolean found = false;
        for(FPTreeNode children : childrens){
            if(children.getKey() == transaction.get( 0 ))
                children.incrementSupport(support);
                double newSupport = itemsSupports.get( children.getKey() ) + support;
                itemsSupports.replace( children.getKey(), newSupport );
            transaction.remove( 0 );
            children.addTransaction( transaction, headerTable, support, itemsSupports);
            found = true;
        }
        if( !found ){
            FPTreeNode newNode = new FPTreeNode(transaction.get( 0 ), null, support);
            headerTable.put( transaction.get( 0 ), new ArrayList < FPTreeNode >(  ) );
            headerTable.get( transaction.get( 0 ) ).add( newNode );
            itemsSupports.put( transaction.get( 0 ), (double)support );
            childrens.add( newNode );
            transaction.remove( 0 );
            newNode.addTransaction( transaction, headerTable , support, itemsSupports);
        }

    }


    public ArrayList < FPTreeNode > getChildrens () {
        return childrens;
    }

    public void setChildrens ( ArrayList < FPTreeNode > childrens ) {
        this.childrens = childrens;
    }

    public HashMap < Integer, ArrayList < FPTreeNode > > getHeaderTable () {
        return headerTable;
    }

    public void setHeaderTable ( HashMap < Integer, ArrayList < FPTreeNode > > headerTable ) {
        this.headerTable = headerTable;
    }

    public TreeMap < Integer, Double > getItemsSupports () {
        return itemsSupports;
    }

    public void setItemsSupports ( TreeMap < Integer, Double > itemsFrequencies ) {
        this.itemsSupports = itemsFrequencies;
    }
}
