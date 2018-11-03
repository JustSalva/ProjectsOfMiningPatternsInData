import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class to manage a dataset stored in a external file.
 *
 * @author Charles Thomas (charles.thomas@uclouvain.be)
 */
public class Dataset{
    private final int[] items; //The different items in the dataset
    private final ArrayList<int[]> transactions; //The transactions in the dataset

    /**
     * Constructor: reads the dataset and initialises fields.
     * @param filePath the path to the dataset file. It is assumed to have the following format:
     *                 Each line corresponds to a transaction. Blank lines might be present and will be ignored.
     *                 Items in a transaction are represented by integers separated by single spaces.
     */
    public Dataset(String filePath) {

        // Counting items and initialising transactions and items structures
        HashSet<Integer> itemsFound = new HashSet<>();
        transactions = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.ready()) {
                String line = reader.readLine();
                if(line.matches("^\\s*$")) continue; //Skipping blank lines
                int[] transaction = Stream.of(line.trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                transactions.add(transaction);
                for(int i: transaction)
                    if(!itemsFound.contains(i)) itemsFound.add(i);
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("Unable to read dataset file!");
            e.printStackTrace();
        }
        items = itemsFound.stream().mapToInt(Number::intValue).toArray();
        Arrays.sort(items);
    }

    /**
     * Returns the number of transactions in the dataset.
     */
    public int transNum() {
        return transactions.size();
    }

    /**
     * Returns the number of different items in the dataset.
     */
    public int itemsNum() {
        return items.length;
    }

    /**
     * Returns an array of all the different items in the dataset
     */
    public int[] items(){
        return items;
    }

    /**
     * Returns the transactions contained in the dataset as an ArrayList of int arrays.
     */
    public ArrayList<int[]> transactions() {
        return transactions;
    }

    /**
     * Returns the transaction at index i as an int array.
     */
    public int[] getTransaction(int i) {
        return transactions.get(i);
    }
}
