package net.seninp.jmotif.sax.motif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.StackTrace;

public class TestMotifDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int MOTIF_SIZE = 100;

  private static final double MOTIF_RANGE = 1.5;

  private static final double ZNORM_THRESHOLD = 0.01;

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    // series = Arrays.copyOf(series, 800);
  }

  @Test
  public void testEMMA() {
    MotifRecord motifsBF;
    MotifRecord motifsEMMA;
    try {

      // Date start = new Date();
      BruteForceMotifImplementation.distCounter = 0;
      BruteForceMotifImplementation.eaCounter = 0;
      motifsBF = BruteForceMotifImplementation.series2BruteForceMotifs(series, MOTIF_SIZE,
          MOTIF_RANGE, ZNORM_THRESHOLD);
      // System.out.println(
      // "brute force: " + SAXProcessor.timeToString(start.getTime(), new Date().getTime()) + " : "
      // + motifsBF + ", dist calls: " + BruteForceMotifImplementation.distCounter
      // + ", early abandoned: " + BruteForceMotifImplementation.eaCounter);

      // start = new Date();
      EMMAImplementation.distCounter = 0;
      EMMAImplementation.eaCounter = 0;
      motifsEMMA = EMMAImplementation.series2EMMAMotifs(series, MOTIF_SIZE, MOTIF_RANGE, 6, 4,
          ZNORM_THRESHOLD);
      // System.out.println("emma: " + SAXProcessor.timeToString(start.getTime(), new
      // Date().getTime())
      // + " : " + motifsEMMA + ", dist calls: " + EMMAImplementation.distCounter
      // + ", early abandoned: " + EMMAImplementation.eaCounter);

      assertEquals("Asserting motif frequency", motifsBF.getFrequency(), motifsEMMA.getFrequency());

      for (Integer m : motifsBF.getOccurrences()) {
        // System.out.println("asserting at " + m);
        assertTrue("Asserting motif locations", motifsEMMA.getOccurrences().contains(m));
      }

      // for (int i = 0; i < 3; i++) {
      // long tstamp0 = System.currentTimeMillis();
      // for (int j = 0; j < 100; j++) {
      // motifsEMMA = EMMAImplementation.series2EMMAMotifs(series, MOTIF_SIZE, MOTIF_RANGE, 5, 5,
      // 0.001);
      // }
      // System.out.println("** " + (System.currentTimeMillis() - tstamp0));
      // }
      //
      // for (int i = 0; i < 3; i++) {
      // long tstamp0 = System.currentTimeMillis();
      // for (int j = 0; j < 100; j++) {
      // motifsBF = BruteForceMotifImplementation.series2BruteForceMotifs(series, MOTIF_SIZE,
      // MOTIF_RANGE, ZNORM_THRESHOLD);
      // }
      // System.out.println("* " + (System.currentTimeMillis() - tstamp0));
      // }

    }
    catch (Exception e) {
      fail("It shouldnt fail, but failed with " + StackTrace.toString(e));
    }
  }
}
