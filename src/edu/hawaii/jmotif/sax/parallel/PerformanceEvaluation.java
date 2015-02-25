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

  private static final Integer NRUNS = 10;

  private static final Integer MIN_CPUS = 2;

  private static final Integer MAX_CPUS = 16;

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

    System.out.println("Will be performing " + NRUNS
        + " SAX runs for each algorithm implementation ... ");

    // conventional
    //
    long tstamp1 = System.currentTimeMillis();
    for (int i = 0; i < NRUNS; i++) {
      @SuppressWarnings("unused")
      SAXRecords sequentialRes2 = sp.ts2saxViaWindow(ts, slidingWindowSize, paaSize,
          na.getCuts(alphabetSize), NumerosityReductionStrategy.EXACT, 0.001);
    }
    long tstamp2 = System.currentTimeMillis();
    System.out.println("conversion with optimized PAA "
        + SAXProcessor.timeToString(tstamp1, tstamp2));

    // parallel
    for (int threadsNum = MIN_CPUS; threadsNum < MAX_CPUS; threadsNum++) {
      tstamp1 = System.currentTimeMillis();
      for (int i = 0; i < NRUNS; i++) {
        ParallelSAXImplementation ps = new ParallelSAXImplementation();
        @SuppressWarnings("unused")
        SAXRecords parallelRes = ps.process(ts, threadsNum, slidingWindowSize, paaSize,
            alphabetSize, NumerosityReductionStrategy.EXACT, 0.005);
      }
      tstamp2 = System.currentTimeMillis();
      System.out.println("parallel conversion using " + threadsNum + " threads: "
          + SAXProcessor.timeToString(tstamp1, tstamp2));
    }
  }

}
