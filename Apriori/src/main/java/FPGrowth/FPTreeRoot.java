package FPGrowth;

import java.util.ArrayList;
import java.util.HashMap;

public class FPTreeRoot {
    private ArrayList< FPTreeNode > childrens;

    public FPTreeRoot ( ) {
        this.childrens = new ArrayList < FPTreeNode >();
    }

    public void addTransaction( ArrayList<Integer> transaction, HashMap<Integer, ArrayList<FPTreeNode>> headerTable){
        boolean found = false;
        for(FPTreeNode children : childrens){
            if(children.getKey() == transaction.get( 0 ))
                children.incrementSupport();
                transaction.remove( 0 );
                children.addTransaction( transaction, headerTable );
                found = true;
        }
        if( !found){
            FPTreeNode newNode = new FPTreeNode(transaction.get( 0 ), null);
            headerTable.get( transaction.get( 0 ) ).add( newNode );
            transaction.remove( 0 );
            newNode.addTransaction( transaction, headerTable );
        }

    }

    public ArrayList < FPTreeNode > getChildrens () {
        return childrens;
    }
}
