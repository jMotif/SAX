package net.seninp.jmotif.sax.motif;

import static org.junit.Assert.fail;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.StackTrace;

public class TestBruteForceMotifDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int MOTIF_SIZE = 100;

  private static final double MOTIF_RANGE = 1.0;

  // logging stuff
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(TestBruteForceMotifDiscovery.class);

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    series = Arrays.copyOf(series, 800);
  }

  @Test
  public void test() {
    MotifRecord motif;
    try {
      motif = BruteForceMotifImplementation.series2BruteForceMotifs(series, MOTIF_SIZE,
          MOTIF_RANGE);
      System.out.println(motif.toString());
    }
    catch (Exception e) {
      fail("It shouldnt fail, but failed with " + StackTrace.toString(e));
    }
  }
}
