package edu.hawaii.jmotif.sax.parallel;

import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXProcessor;
import edu.hawaii.jmotif.sax.TSProcessor;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;

/**
 * This runs the performance evaluation code - we are looking on the speedup.
 * 
 * @author psenin
 * 
 */
public final class PerformanceEvaluation {

  private static final Integer NRUNS = 20;

  private static final Integer MIN_CPUS = 2;

  private static final Integer MAX_CPUS = 16;

  private static final double N_THRESHOLD = 0.001d;

  private PerformanceEvaluation() {
    assert true;
  }

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
    Integer slidingWindowSize = Integer.valueOf(args[1]);
    Integer paaSize = Integer.valueOf(args[2]);
    Integer alphabetSize = Integer.valueOf(args[3]);

    double[] ts = TSProcessor.readFileColumn(dataFileName, 0, 0);
    System.out.println("data file: " + dataFileName);
    System.out.println("data size: " + ts.length);
    System.out.println("SAX parameters: sliding window size " + slidingWindowSize + ", PAA size "
        + paaSize + ", alphabet size " + alphabetSize);

    System.out.println("Performing " + NRUNS
        + " SAX conversion runs for each algorithm implementation ... ");

    // conventional
    //
    long tstamp1 = System.currentTimeMillis();
    for (int i = 0; i < NRUNS; i++) {
      @SuppressWarnings("unused")
      SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, slidingWindowSize, paaSize,
          na.getCuts(alphabetSize), NumerosityReductionStrategy.EXACT, N_THRESHOLD);
    }
    long tstamp2 = System.currentTimeMillis();
    System.out.println("single thread conversion: " + String.valueOf(tstamp2 - tstamp1) + ", "
        + SAXProcessor.timeToString(tstamp1, tstamp2));

    // parallel
    for (int threadsNum = MIN_CPUS; threadsNum < MAX_CPUS; threadsNum++) {
      tstamp1 = System.currentTimeMillis();
      for (int i = 0; i < NRUNS; i++) {
        ParallelSAXImplementation ps = new ParallelSAXImplementation();
        @SuppressWarnings("unused")
        SAXRecords parallelRes = ps.process(ts, threadsNum, slidingWindowSize, paaSize,
            alphabetSize, NumerosityReductionStrategy.EXACT, N_THRESHOLD);
      }
      tstamp2 = System.currentTimeMillis();
      System.out.println("parallel conversion using " + threadsNum + " threads: "
          + String.valueOf(tstamp2 - tstamp1) + ", " + SAXProcessor.timeToString(tstamp1, tstamp2));
    }
  }

}
