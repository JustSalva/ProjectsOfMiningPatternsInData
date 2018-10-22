package Tools;

import FPGrowth.FPTreeNode;

import java.util.ArrayList;
import java.util.Set;

public class Utilities {

    public static int[] combine(int[] list, int element) {
        if(list != null){
            int length = list.length + 1;
            int[] result = new int[ length ];
            System.arraycopy( list, 0, result, 0, list.length );
            result[result.length-1] = element;
            return result;
        }
        else{
            return new int[]{element};
        }
    }

    public static String printElement(int candidateKey, double frequency){
        String toPrint = "["+ candidateKey +"]" ;
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }

    public static String printElement(int[] path, int fatherKey, int candidateKey, double frequency){
        String toPrint = "[";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        toPrint = toPrint.concat( fatherKey + ", "+candidateKey+"]" );
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }

    public static String printElement(int[] path, int key, double frequency){
        String toPrint = "[";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        toPrint = toPrint.concat( key +"]" );
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }

    public static String printElement( int[] previousPath, double frequency, FPTreeNode node,
                                       int numberOfIncludedItems){
        String toPrint = "[";
        for( Integer element :previousPath){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        FPTreeNode tempNode = node;
        for(int i=0; i<=numberOfIncludedItems; i++){
            toPrint= toPrint.concat( Integer.toString( tempNode.getKey()) ).concat(", ");
            tempNode = node.getFather();
        }
        toPrint = toPrint.concat("]" );
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }

    public static String printElement( Set<Integer> path, int key, double frequency){
        String toPrint = "[";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        toPrint = toPrint.concat( key +"]" );
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }
}
