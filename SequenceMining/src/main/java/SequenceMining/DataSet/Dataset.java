package SequenceMining.DataSet;


import SequenceMining.SupportStructures.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toMap;


public class Dataset {
    private LinkedHashMap<String, Integer> items; //The different items in the dataset
    private ArrayList< Transaction > transactions;

    public Dataset ( String filePath, boolean isPositive) {
        items = new LinkedHashMap <>();
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
                    currentTransaction.flushInitializationSupportStructures();
                    currentTransaction = new Transaction(isPositive);
                    i = 0;
                }else{
                    tempElement = line.split( " ")[0];
                    currentTransaction.addTransactionMapping(tempElement ,i );
                    if(items.containsValue( tempElement )){
                        int temp = items.get( tempElement );
                        items.put( tempElement, temp+1 );
                    }else{
                        items.put( tempElement, 1 );
                    }
                    i++;
                }
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
        LinkedHashMap<String, Integer> ordered  = items.entrySet().stream().sorted( Collections.reverseOrder(Map.Entry.comparingByValue())).collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( e1, e2) -> e2, LinkedHashMap::new));
    }

    public LinkedHashMap<String, Integer> getItems () {
        return items;
    }

    public ArrayList < Transaction > getTransactions () {
        return transactions;
    }
}
