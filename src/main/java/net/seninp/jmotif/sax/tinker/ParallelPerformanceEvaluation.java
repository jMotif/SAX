package net.seninp.jmotif.sax.tinker;

import java.util.Arrays;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;

/**
 * This runs the performance evaluation code - we are looking on the speedup.
 * 
 * @author psenin
 * 
 */
public final class ParallelPerformanceEvaluation {

  private static final Integer NRUNS = 3;

  private static final Integer MIN_CPUS = 2;

  private static final Integer MAX_CPUS = 16;

  private static final double N_THRESHOLD = 0.001d;

  private static final int[] WINDOWS = { 100, 200, 300 };
  private static final int[] PAAS = { 5, 9, 13 };
  private static final int[] ALPHABETS = { 3, 5, 7 };
  private static final String[] NRS = { "NONE", "EXACT", "MINDIST" };

  /**
   * Runs the evaluation.
   * 
   * @param args some accepted, see the code.
   * @throws Exception thrown if an error occured.
   */
  public static void main(String[] args) throws Exception {

    NormalAlphabet na = new NormalAlphabet();
    SAXProcessor sp = new SAXProcessor();

    String dataFileName = args[0];

    System.out.println("data file: " + dataFileName);
    double[] ts = TSProcessor.readFileColumn(dataFileName, 0, 0);
    System.out.println("data size: " + ts.length);
    System.out.println("SAX parameters:\n sliding window sizes: " + Arrays.toString(WINDOWS)
        + "\n PAA sizes: " + Arrays.toString(PAAS) + "\n alphabet sizes: "
        + Arrays.toString(ALPHABETS) + "\n NR strategis: " + Arrays.toString(NRS));

    System.out.println(
        "Performing " + NRUNS + " SAX conversion runs for each algorithm implementation ... ");

    // conventional
    //
    long tstamp1 = System.currentTimeMillis();
    for (int slidingWindowSize : WINDOWS) {
      for (int paaSize : PAAS) {
        for (int alphabetSize : ALPHABETS) {
          for (String nrStrategy : NRS) {

            for (int i = 0; i < NRUNS; i++) {
              @SuppressWarnings("unused")
              SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, slidingWindowSize, paaSize,
                  na.getCuts(alphabetSize), NumerosityReductionStrategy.fromString(nrStrategy),
                  N_THRESHOLD);
            }

          }
        }
      }
    }
    long tstamp2 = System.currentTimeMillis();
    System.out.println("single thread conversion: " + String.valueOf(tstamp2 - tstamp1) + ", "
        + SAXProcessor.timeToString(tstamp1, tstamp2));

    // parallel
    for (int threadsNum = MIN_CPUS; threadsNum < MAX_CPUS; threadsNum++) {
      tstamp1 = System.currentTimeMillis();

      for (int slidingWindowSize : WINDOWS) {
        for (int paaSize : PAAS) {
          for (int alphabetSize : ALPHABETS) {
            for (String nrStrategy : NRS) {

              for (int i = 0; i < NRUNS; i++) {
                ParallelSAXImplementation ps = new ParallelSAXImplementation();
                @SuppressWarnings("unused")
                SAXRecords parallelRes = ps.process(ts, threadsNum, slidingWindowSize, paaSize,
                    alphabetSize, NumerosityReductionStrategy.fromString(nrStrategy), N_THRESHOLD);
              }

            }
          }
        }
      }

      tstamp2 = System.currentTimeMillis();
      System.out.println("parallel conversion using " + threadsNum + " threads: "
          + String.valueOf(tstamp2 - tstamp1) + ", " + SAXProcessor.timeToString(tstamp1, tstamp2));
    }
  }

}
