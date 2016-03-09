package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.util.StdRandom;

/**
 * Test the tsp.
 * 
 * @author Pavel Senin.
 * 
 * 
 * 
 * 
 */
public class TestPAAPerf {

  private static final String ts1File = "src/resources/test-data/ann_gun_CentroidA1.csv";

  // logging stuff
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(TestPAAPerf.class);

  TSProcessor tsp;

  /**
   * Test the extremum calculations.
   * 
   * @throws IOException
   * @throws SAXException
   */
  @SuppressWarnings("deprecation")
  @Test
  public void test() throws SAXException, IOException {

    tsp = new TSProcessor();
    double[] ts1 = tsp.readTS(ts1File, 150000);

    long time1 = 0;
    long time2 = 0;
    long time3 = 0;

    for (int i = 0; i < 10; i++) {

      int paaNum = StdRandom.uniform(10, ts1.length / 10);
      LOGGER.debug("iteration: " + i + ", paa size: " + paaNum + "; ");

      Date start1 = new Date();
      double[] paa1 = tsp.paa_oldest(ts1, paaNum);
      Date end1 = new Date();

      Date start2 = new Date();
      double[] paa2 = tsp.paa_old(ts1, paaNum);
      Date end2 = new Date();

      Date start3 = new Date();
      double[] paa3 = tsp.paa(ts1, paaNum);
      Date end3 = new Date();

      for (int j = 0; j < paa2.length; j++) {
        assertEquals("PAA", paa2[j], paa1[j], 0.000001);
      }

      for (int j = 0; j < paa2.length; j++) {
        assertEquals("PAA", paa2[j], paa3[j], 0.000001);
      }

      time1 += end1.getTime() - start1.getTime();
      time2 += end2.getTime() - start2.getTime();
      time3 += end3.getTime() - start3.getTime();

    }

    LOGGER.debug("\n ** paa perf. old " + time1 + ", new: " + time2 + ", newest: " + time3 + "\n");

  }

}
