package edu.hawaii.jmotif.sax.parallel;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXProcessor;
import edu.hawaii.jmotif.sax.TSProcessor;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;

public class TestParallelSAXImplementation {

  private static final String ts1File = "test/data/timeseries01.csv";
  private static final String ts1StrRep10 = "bcjkiheebb";
  private static final String ts1StrRep14 = "bcdijjhgfeecbb";
  private static final String ts1StrRep9 = "bcggfddba";
  private static final int ts1Length = 15;

  private static final String filenameTEK14 = "test/data/TEK14.txt";

  private static final int THREADS_NUM = 5;

  private static final int WINDOW_SIZE = 128;
  private static final int PAA_SIZE = 7;
  private static final int ALPHABET_SIZE = 5;

  private static final double NORM_THRESHOLD = 0.0005d;

  private static double[] ts1;

  @Before
  public void setUp() throws Exception {
    ts1 = TSProcessor.readFileColumn(ts1File, 0, ts1Length);
  }

  // /**
  // * Test the simple SAX conversion.
  // *
  // * @throws Exception if error occurs.
  // */
  // @Test
  // public void testSAXWorker() throws Exception {
  //
  // ExecutorService executorService = Executors.newFixedThreadPool(1);
  // CompletionService<SAXRecords> completionService = new ExecutorCompletionService<SAXRecords>(
  // executorService);
  //
  // int totalTaskCounter = 0;
  //
  // long tstamp = System.currentTimeMillis();
  //
  // final SAXWorker job1 = new SAXWorker(tstamp + totalTaskCounter, ts1, 0, ts1.length, ts1.length,
  // 10, 11, NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
  // completionService.submit(job1);
  // totalTaskCounter++;
  //
  // final SAXWorker job2 = new SAXWorker(tstamp + totalTaskCounter, ts1, 0, ts1.length, ts1.length,
  // 14, 10, NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
  // completionService.submit(job2);
  // totalTaskCounter++;
  //
  // final SAXWorker job3 = new SAXWorker(tstamp + totalTaskCounter, ts1, 0, ts1.length, ts1.length,
  // 9, 7, NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
  //
  // completionService.submit(job3);
  // totalTaskCounter++;
  //
  // executorService.shutdown();
  //
  // try {
  // while (totalTaskCounter > 0) {
  // Future<SAXRecords> finished = completionService.poll(2, TimeUnit.HOURS);
  // if (null == finished) {
  // // something went wrong - break from here
  // System.err.println("Breaking POLL loop after 1 HOUR of waiting...");
  // break;
  // }
  // else {
  // SAXRecords res = finished.get();
  // if (tstamp + 0 == res.getId()) {
  // String ts1sax = String.valueOf(res.getByIndex(0).getPayload());
  // assertEquals("testing SAX", 10, ts1sax.length());
  // assertTrue("testing SAX", ts1StrRep10.equalsIgnoreCase(ts1sax));
  // totalTaskCounter--;
  // }
  // else if (tstamp + 1 == res.getId()) {
  // String ts1sax = String.valueOf(res.getByIndex(0).getPayload());
  // assertEquals("testing SAX", 14, ts1sax.length());
  // assertTrue("testing SAX", ts1StrRep14.equalsIgnoreCase(ts1sax));
  // totalTaskCounter--;
  // }
  // else if (tstamp + 2 == res.getId()) {
  // String ts1sax = String.valueOf(res.getByIndex(0).getPayload());
  // assertEquals("testing SAX", 9, ts1sax.length());
  // assertTrue("testing SAX", ts1StrRep9.equalsIgnoreCase(ts1sax));
  // totalTaskCounter--;
  // }
  // }
  // }
  //
  // }
  // catch (Exception e) {
  // System.err.println("Error while waiting results: " + StackTrace.toString(e));
  // }
  // finally {
  // // wait at least 1 more hour before terminate and fail
  // try {
  // if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
  // executorService.shutdownNow(); // Cancel currently executing tasks
  // if (!executorService.awaitTermination(30, TimeUnit.MINUTES))
  // System.err.println("Pool did not terminate... FATAL ERROR");
  // }
  // }
  // catch (InterruptedException ie) {
  // System.err.println("Error while waiting interrupting: " + StackTrace.toString(ie));
  // // (Re-)Cancel if current thread also interrupted
  // executorService.shutdownNow();
  // // Preserve interrupt status
  // Thread.currentThread().interrupt();
  // }
  //
  // }
  // }

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
    System.out.println(str1);

    String sequentialString = sequentialRes.getSAXString(" ");
    // 3 threads
    ParallelSAXImplementation ps1 = new ParallelSAXImplementation();
    SAXRecords parallelRes = ps1.process(ts, THREADS_NUM, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, NORM_THRESHOLD);

    String str2 = parallelRes.getSAXString(" ");
    System.out.println(str2);

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
      // System.out.println("index: " + i + ", serial: " + entrySerial + ", parallel: "
      // + entryParallel);
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

    // System.out.println(sequentialString);
    // System.out.println(sequentialRes.getAllIndices());
    // System.out.println(sequentialString2);
    // System.out.println(sequentialRes2.getAllIndices());
    // System.out.println(parallelRes.getSAXString(" "));
    // System.out.println(Arrays.toString(parallelRes.getAllIndices().toArray(
    // new Integer[parallelRes.getAllIndices().size()])));

  }
}
