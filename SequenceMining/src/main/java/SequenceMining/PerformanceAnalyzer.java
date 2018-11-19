package SequenceMining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

public class PerformanceAnalyzer {
    private static final long MEGABYTE = 1024L * 1024L;
    private static String[] filesPositive= {"Test/positive.txt", "Protein/PKA_group15.txt","Reuters/acq.txt" };
    private static String[] filesNegative = {"Test/negative.txt", "Protein/SRC1521.txt","Reuters/earn.txt"};
    private static String[] datasets = {"Test", "Protein","Reuters"};
    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File("PrefixSpan_result.txt");
        PrintWriter writer = new PrintWriter("PrefixSpan_result.txt", "UTF-8");
        String filePath;
        for(int dataset = 0; dataset < filesPositive.length; dataset++){
            for( int kValue = 1; kValue<= 10 ; kValue ++ ){
                writer.println( datasets[dataset] );
                filePath = "/mnt/Shared/GitHub/ProjectsOfMiningPatternsInData/SequenceMining/src/main/resources" +
                        "/Datasets/Datasets/";
                String[] arguments = {filePath + filesPositive[dataset], filePath + filesNegative[dataset],
                        Integer.toString( kValue )};
                long startTime = System.currentTimeMillis();
                writer.print( PrefixSpan.performances( arguments ));
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                writer.println( "kValue: " +  " elapsedTime: ");
                writer.println( kValue +  " " + elapsedTime );
                writer.println( "");
                writer.flush();

            }
        }
        writer.close();


    }

}
