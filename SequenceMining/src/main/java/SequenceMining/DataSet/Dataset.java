package SequenceMining.DataSet;


import SequenceMining.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


public class Dataset {
    private HashSet<String>  items; //The different items in the dataset
    private ArrayList< Transaction > transactions;

    public Dataset ( String filePath, boolean isPositive) {
        items = new HashSet<>();
        transactions = new ArrayList<>();
        try {
            Transaction currentTransaction = new Transaction(isPositive);
            int i = 0;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String tempElement;
            reader.readLine();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.equals( "") ){
                    transactions.add( currentTransaction );
                    currentTransaction = new Transaction(isPositive);
                    i = 0;
                }else{
                    tempElement = line.split( " ")[0];
                    currentTransaction.addTransactionMapping(tempElement ,i );
                    items.add(tempElement);
                    i++;
                }
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
    }

    public HashSet<String> getItems () {
        return items;
    }

    public ArrayList < Transaction > getTransactions () {
        return transactions;
    }
}
