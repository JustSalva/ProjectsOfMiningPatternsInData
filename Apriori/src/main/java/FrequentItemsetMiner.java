/**
 * Skeleton class for the project 1 of the LINGI2364 course.
 * Use this as main class of your jar file.
 *
 * This class and the Dataset classes are given to you as a skeleton for your implementation of the Apriori and Depth
 * First Search algorithms. You are not obligated to use them and are free to write any class or method as long as the
 * following requirements are respected:
 *
 * Your apriori and alternativeMiner methods must take as parameters a string corresponding to the path to a valid
 * dataset file and a double corresponding to the minimum frequency.
 * You must write on the standard output (System.out) all the itemsets that are frequent in the dataset file according
 * to the minimum frequency given. Each itemset has to be printed on one line following the format:
 * [<item 1>, <item 2>, ... <item k>] (<frequency>).
 * Tip: you can use Arrays.toString(int[] a) to print an itemset.
 *
 * The items in an itemset must be printed in lexicographical order. However, the itemsets themselves can be printed in
 * any order.
 *
 * The main method will be the one called by the test script. Do not change it.
 *
 * @author <write here your group, first name(s) and last name(s)>
 */
public class FrequentItemsetMiner{

    /**
     * This method will be called by the grading script. Please do not change it.
     * @param args the command line arguments for the program:
     *             1) A String which is the path to the dataset file.
     *             2) A Double which is the minFrequency value to be used.
     *             3) A String whose value is either "apriori" or "alternative" to indicate which method to run.
     */
    public static void main(String[] args) {
        if(args.length != 3) System.out.println("Incorrect number of arguments! Aborting execution.");
        else{
            String filepath = args[0];
            Double minFrequency = Double.parseDouble(args[1]);
            String miner = args[2];
            switch (miner){
                case "apriori": apriori(filepath, minFrequency);
                    break;
                case "alternative": alternativeMiner(filepath, minFrequency);
                    break;
            }
        }
    }

    /**
     * Prints all the frequent (i.e. having a frequency >= minFrequency) itemsets in filepath.
     * @param filepath The path to a valid dataset file.
     * @param minFrequency the minimum frequency for an itemset to be considered as frequent.
     */
    public static void apriori(String filepath, double minFrequency){
        //TODO: implementation of the apriori algorithm
    }

    /**
     * Prints all the frequent (i.e. having a frequency >= minFrequency) itemsets in filepath.
     * @param filepath The path to a valid dataset file.
     * @param minFrequency the minimum frequency for an itemset to be considered as frequent.
     */
    public static void alternativeMiner(String filepath, double minFrequency){
        //TODO: either second implementation of the apriori algorithm or implementation of the depth first search algorithm
    }
}