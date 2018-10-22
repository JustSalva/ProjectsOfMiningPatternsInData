package Tools;

import FPGrowth.FPTreeNode;
import FPGrowth.FPTreeRoot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class FPGrowthDataSet {

    private TreeMap<Integer, Double> itemsFound ; //The different items in the dataset
    private double numberOfTransactions;
    private FPTreeRoot root;
    private HashMap<Integer, ArrayList< FPTreeNode >> headerTable;

    public FPGrowthDataSet ( String filePath, double minFrequency) {
        // Counting items and initialising transactions and items structures
        this.itemsFound = new TreeMap<Integer, Double>();
        this.numberOfTransactions = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.ready()) {
                String line = reader.readLine();
                if ( line.matches( "^\\s*$" ) ) continue; //Skipping blank lines
                int[] transaction = Stream.of( line.trim().split( " " ) ).mapToInt( Integer::parseInt ).toArray();
                this.numberOfTransactions ++;
                for ( int i : transaction )
                    if ( !itemsFound.containsKey( i ) ) itemsFound.put( i, 1.0 );
                    else itemsFound.replace( i, itemsFound.get( i ) + 1.0 );
            }
            reader.close();
        }
        catch ( IOException e) {
            System.err.println( "Unable to read dataset file!" );
            e.printStackTrace();
        }
        this.root = new FPTreeRoot();
        this.headerTable = new HashMap <>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.ready()) {
                String line = reader.readLine();
                if ( line.matches( "^\\s*$" ) ) continue; //Skipping blank lines
                int[] databaseLine = Stream.of( line.trim().split( " " ) ).mapToInt( Integer::parseInt ).toArray();
                ArrayList<Integer> transaction = new ArrayList<Integer>();
                for ( int element : databaseLine ){
                    if( itemsFound.get( element ) / numberOfTransactions >=  minFrequency){
                        transaction.add( element );
                    }
                }
                Collections.sort(transaction, new Comparator<Integer>(){
                    public int compare(Integer firstItem, Integer secondItem){
                        // compare the frequency
                        int compare = (int)(itemsFound.get(secondItem) - itemsFound.get(firstItem));
                        // if the same frequency, we check the lexical ordering!
                        if(compare == 0){
                            return (firstItem - secondItem);
                        }
                        // otherwise, just use the frequency
                        return compare;
                    }
                });
                root.addTransaction( transaction, headerTable);

            }


            reader.close();
        }
        catch ( IOException e) {
            System.err.println( "Unable to read dataset file!" );
            e.printStackTrace();
        }


    }

    public TreeMap < Integer, Double > getItemsFound () {
        return itemsFound;
    }

    public double getNumberOfTransactions () {
        return numberOfTransactions;
    }

    public FPTreeRoot getRoot () {
        return root;
    }

    public HashMap < Integer, ArrayList < FPTreeNode > > getHeaderTable () {
        return headerTable;
    }
}
