package net.seninp.jmotif.sax.parallel;

import static org.junit.Assert.assertTrue;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import org.junit.Test;

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
  public void testParallelSAX() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(TEST_DATA, 0, 0);

    SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);

    String sequentialString = sequentialRes.getSAXString(" ");

    for (int threadsNum : THREADS_NUM) {
      ParallelSAXImplementation ps1 = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps1.process(ts, threadsNum, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);

      String str2 = parallelRes.getSAXString(" ");

      String[] arr1 = sequentialString.split(" ");
      String[] arr2 = str2.split(" ");

      for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
        if (!arr1[i].equalsIgnoreCase(arr2[i])) {
          System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]);
          break;
        }
      }

      assertTrue("assert correctness", sequentialString.equalsIgnoreCase(str2));

    }

  }
}
