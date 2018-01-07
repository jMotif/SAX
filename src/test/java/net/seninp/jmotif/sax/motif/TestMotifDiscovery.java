package net.seninp.jmotif.sax.motif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.StackTrace;

public class TestMotifDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int MOTIF_SIZE = 100;

  private static final double MOTIF_RANGE = 2.;

  private static final double ZNORM_THRESHOLD = 0.001;

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    series = Arrays.copyOf(series, 800);
  }

  @Test
  public void testEMMA() {
    MotifRecord motifsBF;
    MotifRecord motifsEMMA;
    try {

      motifsBF = BruteForceMotifImplementation.series2BruteForceMotifs(series, MOTIF_SIZE,
          MOTIF_RANGE, ZNORM_THRESHOLD);
      // System.out.println(motifsBF);

      motifsEMMA = EMMAImplementation.series2EMMAMotifs(series, MOTIF_SIZE, MOTIF_RANGE, 5, 5,
          0.001);
       System.out.println(motifsEMMA);

      assertEquals("Asserting motif frequency", motifsBF.getFrequency(), motifsEMMA.getFrequency());

      for (Integer m : motifsBF.getOccurrences()) {
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
