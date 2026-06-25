package net.seninp.jmotif.sax.parallel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
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
   * Asserts that two SAX results are identical as full index-&gt;word maps: same set of indices and
   * the same word at every index. This is stronger than the legacy split-on-space comparison, which
   * only checked the first min(len1,len2) words by position and therefore silently accepted a
   * divergent word count (trailing truncation or trailing extra words at a seam).
   *
   * @param expected the reference (sequential) result.
   * @param actual the parallel result.
   * @param context a label for assertion messages.
   */
  private static void assertSameMap(SAXRecords expected, SAXRecords actual, String context) {
    ArrayList<Integer> expKeys = expected.getAllIndices();
    ArrayList<Integer> actKeys = actual.getAllIndices();
    assertEquals(context + ": word count must match", expKeys.size(), actKeys.size());
    assertEquals(context + ": index set must match", expKeys, actKeys);
    for (int i : expKeys) {
      String e = String.valueOf(expected.getByIndex(i).getPayload());
      String a = String.valueOf(actual.getByIndex(i).getPayload());
      assertEquals(context + ": word at index " + i, e, a);
    }
  }

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

  /**
   * Strict equality: parallel must reproduce the sequential result as an exact index-&gt;word map
   * (same keys, same words, same count) for every strategy and thread count -- including the
   * single-threaded (threadsNum == 1) rollback path. This catches word-count divergences at seams
   * that the legacy split-on-space comparisons miss.
   *
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXStrictMapEquality() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    ParallelSAXImplementation ps = new ParallelSAXImplementation();

    for (NumerosityReductionStrategy strategy : NumerosityReductionStrategy.values()) {

      SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
          na.getCuts(ALPHABET_SIZE), strategy, NORM_THRESHOLD);

      for (int threadsNum = 1; threadsNum <= 8; threadsNum++) {
        SAXRecords parallelRes = ps.process(ts, threadsNum, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
            strategy, NORM_THRESHOLD);
        assertSameMap(sequentialRes, parallelRes,
            "strategy=" + strategy + ", threads=" + threadsNum);
      }
    }
  }

  /**
   * Order-independence regression test. The chunk results merge in nondeterministic completion
   * order, so a merge that relies on completion order can produce a different result run-to-run.
   * This is most easily triggered when the same SAX word repeats across several consecutive chunk
   * boundaries (e.g. a near-constant series), where an incremental seam dedup can leave a duplicate
   * behind on some completion orders but not others. We run each configuration many times so that a
   * variety of completion orders are exercised, and assert that every run reproduces the sequential
   * result exactly.
   *
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAXOrderIndependence() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    // a near-constant series: almost every window maps to the same word, so chunk seams repeatedly
    // land on identical words -- the worst case for an order-dependent seam dedup.
    double[] flat = new double[2299];
    java.util.Random rnd = new java.util.Random(42);
    for (int i = 0; i < flat.length; i++) {
      flat[i] = 5.0 + rnd.nextDouble() * 1e-4;
    }

    final int window = 160;
    final int repeats = 20;

    ParallelSAXImplementation ps = new ParallelSAXImplementation();

    for (NumerosityReductionStrategy strategy : NumerosityReductionStrategy.values()) {

      SAXRecords sequentialRes = sp.ts2saxViaWindow(flat, window, PAA_SIZE, na.getCuts(ALPHABET_SIZE),
          strategy, NORM_THRESHOLD);

      for (int threadsNum = 2; threadsNum <= 8; threadsNum++) {
        for (int rep = 0; rep < repeats; rep++) {
          SAXRecords parallelRes = ps.process(flat, threadsNum, window, PAA_SIZE, ALPHABET_SIZE,
              strategy, NORM_THRESHOLD);
          assertSameMap(sequentialRes, parallelRes,
              "strategy=" + strategy + ", threads=" + threadsNum + ", rep=" + rep);
        }
      }
    }
  }
}
