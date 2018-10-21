package Tools;

import java.util.LinkedHashSet;
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
    public static String printElement( Set<Integer> path, int key, double frequency){
        String toPrint = "[";
        for( Integer element :path){
            toPrint= toPrint.concat( element.toString() ).concat(", ");
        }
        toPrint = toPrint.concat( key +"]" );
        return toPrint.concat( "  ("+ Double.toString( frequency ) +")" + "\n");
    }
}
