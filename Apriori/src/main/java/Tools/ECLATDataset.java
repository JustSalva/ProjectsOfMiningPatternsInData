package Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Stream;
import static Tools.Utilities.combine;


public class ECLATDataset{
    private final TreeMap<Integer, int[] > verticalRepresentation;
    private final int transactionNumber;

    public ECLATDataset(String filePath) {
        int transactionNumber = 1;
        int temp;
        verticalRepresentation = new TreeMap <>();
        // Counting items and initialising transactions and items structures
        ArrayList<int[]> database = new ArrayList <>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.ready()) {
                String line = reader.readLine();
                if(line.matches("^\\s*$")) continue; //Skipping blank lines
                int[] transaction = Stream.of(line.trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                database.add( transaction );
            }

            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
        for(int[] transaction : database) {
            for ( int i = 0; i < transaction.length; i++ ) {
                if ( !verticalRepresentation.containsKey( transaction[ i ] ) ) {
                    verticalRepresentation.put( transaction[ i ], new int[]{} );
                }

                verticalRepresentation.replace( transaction[ i ],
                        combine( verticalRepresentation.get( transaction[ i ] ), transactionNumber ) );
            }
            transactionNumber++;
        }
        this.transactionNumber = transactionNumber;
    }

    public TreeMap < Integer, int[] > getVerticalRepresentation () {
        return verticalRepresentation;
    }

    public int getTransactionNumber () {
        return transactionNumber;
    }
}
