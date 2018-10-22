import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class PerformancesAnalyzer {
    private static final long MEGABYTE = 1024L * 1024L;
    private static String[] files= {"pumsb.dat"};

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File("ECLAT_result5.txt");
        PrintWriter writer = new PrintWriter("ECLAT_result5.txt", "UTF-8");

        for( double minFrequency = 0.8; minFrequency>= 0.05; minFrequency -= 0.05 ){
            for(String filePath : files){
                writer.println( filePath );
                filePath = "/mnt/Shared/GitHub/ProjectsOfMiningPatternsInData/Apriori/src/main/resources/datasets" +
                        "/Datasets/" + filePath;
                // I assume you will know how to create a object Person yourself...
                long startTime = System.currentTimeMillis();

                FrequentItemsetMiner.alternativeMiner( filePath, minFrequency );

                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                // Get the Java runtime
                //Runtime runtime = Runtime.getRuntime();
                // Run the garbage collector
                //runtime.gc();
                // Calculate the used memory
                //long memory = runtime.totalMemory() - runtime.freeMemory();
                writer.println( minFrequency +  " " + elapsedTime +  " " + getTotalMemory());
                writer.flush();
                long before = getGcCount();
                System.gc();
                while (getGcCount() == before);
            }


        }
        writer.close();


    }

    static long getGcCount() {
        long sum = 0;
        for ( GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) { sum +=  count; }
        }
        return sum;
    }

    static long getTotalMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }
}
