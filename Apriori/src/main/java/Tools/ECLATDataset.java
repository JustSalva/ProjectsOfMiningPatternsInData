package Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Stream;

public class ECLATDataset{
    private final TreeMap<Integer, ArrayList<Integer> > verticalRepresentation;
    private final int transactionNumber;

    public ECLATDataset(String filePath) {
        int transactionNumber = 1;
        int temp[];
        verticalRepresentation = new TreeMap <>();
        // Counting items and initialising transactions and items structures
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.ready()) {
                String line = reader.readLine();
                if(line.matches("^\\s*$")) continue; //Skipping blank lines
                int[] transaction = Stream.of(line.trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                for(int i = 0; i < transaction.length; i++) {
                    if ( !verticalRepresentation.containsKey( transaction[ i ] ) ) {
                        temp = new ArrayList <>();
                        verticalRepresentation.put( transaction[ i ], temp );
                    }
                    verticalRepresentation.get( transaction[ i ] ).add( transactionNumber );
                }
                transactionNumber ++;
            }

            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
        this.transactionNumber = transactionNumber;
    }

    public TreeMap < Integer, ArrayList < Integer > > getVerticalRepresentation () {
        return verticalRepresentation;
    }

    public int getTransactionNumber () {
        return transactionNumber;
    }
}
