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

  private static final String filenameTEK14 = "src/test/test/data/TEK14.txt";

  private static final int THREADS_NUM = 5;

  private static final int WINDOW_SIZE = 128;
  private static final int PAA_SIZE = 7;
  private static final int ALPHABET_SIZE = 5;

  private static final double NORM_THRESHOLD = 0.0005d;

  /**
   * Test parallel SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testParallelSAX() throws Exception {

    SAXProcessor sp = new SAXProcessor();
    NormalAlphabet na = new NormalAlphabet();

    double[] ts = TSProcessor.readFileColumn(filenameTEK14, 0, 0);

    SAXRecords sequentialRes = sp.ts2saxViaWindow(ts, WINDOW_SIZE, PAA_SIZE,
        na.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);

    String str1 = sequentialRes.getSAXString(" ");
    // System.out.println(str1);

    String sequentialString = sequentialRes.getSAXString(" ");
    // 3 threads
    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();
    SAXRecords parallelRes = ps1.process(ts, THREADS_NUM, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);

    String str2 = parallelRes.getSAXString(" ");
    // System.out.println(str2);

    String[] arr1 = str1.split(" ");
    String[] arr2 = str2.split(" ");

    for (int i = 0; i < Math.min(arr1.length, arr2.length); i++) {
      if (!arr1[i].equalsIgnoreCase(arr2[i])) {
        System.out.println("Error in index " + i + ", string " + arr1[i] + " versus " + arr2[i]);
        break;
      }
    }

    assertTrue(sequentialString.equalsIgnoreCase(parallelRes.getSAXString(" ")));

    for (int i : parallelRes.getIndexes()) {
      String entrySerial = String.valueOf(sequentialRes.getByIndex(i).getPayload());
      String entryParallel = String.valueOf(parallelRes.getByIndex(i).getPayload());
      assertTrue(entrySerial.equalsIgnoreCase(entryParallel));
    }

    SAXRecords sequentialRes2 = sp.ts2saxViaWindow(ts, 100, 8, na.getCuts(4),
        NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
    String sequentialString2 = sequentialRes2.getSAXString(" ");
    // 3 threads
    ParallelSAXImplementation ps2 = new ParallelSAXImplementation();
    SAXRecords parallelRes2 = ps2.process(ts, THREADS_NUM, 100, 8, 4,
        NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);
    assertTrue(sequentialString2.equalsIgnoreCase(parallelRes2.getSAXString(" ")));

    for (int i : parallelRes2.getIndexes()) {
      String entrySerial = String.valueOf(sequentialRes2.getByIndex(i).getPayload());
      String entryParallel = String.valueOf(parallelRes2.getByIndex(i).getPayload());
      assertTrue(entrySerial.equalsIgnoreCase(entryParallel));
    }

  }
}
