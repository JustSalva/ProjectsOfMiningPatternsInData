package DataSet;

import PrefixSpan.TransactionPrefixSpan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class DatasetPrefixSpan {
    private ArrayList<String> items; //The different items in the dataset
    private ArrayList< TransactionPrefixSpan > transactions;

    public DatasetPrefixSpan ( String filePath) {
        items = new ArrayList <>();
        transactions = new ArrayList<>();
        HashSet<String> itemsFound = new HashSet<>();
        try {
            TransactionPrefixSpan currentTransaction = new TransactionPrefixSpan();
            int i = 0;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String tempElement;
            reader.readLine();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.equals( "") ){
                    transactions.add( currentTransaction );
                    currentTransaction = new TransactionPrefixSpan();
                    i = 0;
                }else{
                    tempElement = line.split( " ")[0];
                    currentTransaction.addTransactionMapping(tempElement ,i );
                    itemsFound.add(tempElement);
                    i++;
                }
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
        items = new ArrayList <>( itemsFound );
        Collections.sort( items );
    }

    public ArrayList < String > getItems () {
        return items;
    }

    public ArrayList < TransactionPrefixSpan > getTransactions () {
        return transactions;
    }
}
