package net.seninp.jmotif.sax.parallel;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Testing the parallel SAX implementation.
 * 
 * @author psenin
 * 
 */
public class TestParallelSAXImplementation {

  private static final String TEST_DATA = "src/resources/test-data/ecg0606_1.csv";

  private static final int[] THREADS_NUM = { 2, 3, 4, 5 };

  private static final int WINDOW_SIZE = 100;
  private static final int PAA_SIZE = 4;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.001;

  /**
   * Test parallel SAX conversion.
   *
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXNONE() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    // test EXACT
    //
    SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
    String sequentialString = sequentialRes.getSAXString(" ");

    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();

    for (int threadsNum : THREADS_NUM) {
      SAXRecords parallelRes = ps1.process(ts, threadsNum, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
      String parallelStr = parallelRes.getSAXString(" ");

      if (sequentialString.equalsIgnoreCase(parallelStr)) {
        assertTrue("assert correctness", sequentialString.equalsIgnoreCase(parallelStr));
      }
      else {
        String[] arr1 = sequentialString.split(" ");
        String[] arr2 = parallelStr.split(" ");
        for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
          if (!arr1[i].equalsIgnoreCase(arr2[i])) {
            System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]
                + ", threads: " + threadsNum);
            assertTrue("assert correctness", arr1[i].equalsIgnoreCase(arr2[i]));
          }
        }
      }
    }
  }

  /**
   * Test parallel SAX conversion.
   *
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXExact() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    // test EXACT
    //
    SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
    String sequentialString = sequentialRes.getSAXString(" ");

    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();

    for (int threadsNum : THREADS_NUM) {
      SAXRecords parallelRes = ps1.process(ts, threadsNum, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
      String parallelStr = parallelRes.getSAXString(" ");

      if (sequentialString.equalsIgnoreCase(parallelStr)) {
        assertTrue("assert correctness", sequentialString.equalsIgnoreCase(parallelStr));
      }
      else {
        String[] arr1 = sequentialString.split(" ");
        String[] arr2 = parallelStr.split(" ");
        for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
          if (!arr1[i].equalsIgnoreCase(arr2[i])) {
            System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]
                + ", threads: " + threadsNum);
            assertTrue("assert correctness", arr1[i].equalsIgnoreCase(arr2[i]));
          }
        }
      }
    }
  }

  /**
   * Test parallel SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXMINDIST() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    // test EXACT
    //
    SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.MINDIST, NORM_THRESHOLD);
    String sequentialString = sequentialRes.getSAXString(" ");

    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();

    for (int threadsNum : THREADS_NUM) {
      SAXRecords parallelRes = ps1.process(ts, threadsNum, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          NumerosityReductionStrategy.MINDIST, NORM_THRESHOLD);
      String parallelStr = parallelRes.getSAXString(" ");

      if (sequentialString.equalsIgnoreCase(parallelStr)) {
        assertTrue("assert correctness", sequentialString.equalsIgnoreCase(parallelStr));
      }
      else {
        String[] arr1 = sequentialString.split(" ");
        String[] arr2 = parallelStr.split(" ");
        for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
          if (!arr1[i].equalsIgnoreCase(arr2[i])) {
            System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]
                + ", threads: " + threadsNum);
            assertTrue("assert correctness", arr1[i].equalsIgnoreCase(arr2[i]));
          }
        }
      }

    }
  }

  /**
   * Test parallel SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXrollbac() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    int threadsNum = 10;
    int slidingWindowSize = 300; // so itll be eneven...

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    // test MINDIST
    //
    SAXRecords sequentialResMINDIST = sp.ts2saxViaWindow(ts, slidingWindowSize, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
    String sequentialStringMINDIST = sequentialResMINDIST.getSAXString(" ");

    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();
    SAXRecords parallelRes = ps1.process(ts, threadsNum, slidingWindowSize, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
    String parallelStr = parallelRes.getSAXString(" ");

    if (sequentialStringMINDIST.equalsIgnoreCase(parallelStr)) {
      assertTrue("assert correctness", sequentialStringMINDIST.equalsIgnoreCase(parallelStr));
    }
    else {
      String[] arr1 = sequentialStringMINDIST.split(" ");
      String[] arr2 = parallelStr.split(" ");
      for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
        if (!arr1[i].equalsIgnoreCase(arr2[i])) {
          System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]
              + ", threads: " + threadsNum);
          assertTrue("assert correctness", arr1[i].equalsIgnoreCase(arr2[i]));
        }
      }
    }

  }
}
