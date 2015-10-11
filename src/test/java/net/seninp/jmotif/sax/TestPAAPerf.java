package net.seninp.jmotif.sax;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import org.junit.Test;

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

  private static final String ts1File = "src/resources/test-data/300_signal1.txt";

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
    double[] ts1 = tsp.readTS(ts1File, 0);

    Date start1 = new Date();
    double[] paa1 = tsp.paa_old(ts1, 59664);
    Date end1 = new Date();

    Date start2 = new Date();
    double[] paa2 = tsp.paa(ts1, 59664);
    Date end2 = new Date();

    assertTrue(Arrays.equals(paa1, paa2));

    System.out
        .println("paa perf. old " + SAXProcessor.timeToString(start1.getTime(), end1.getTime())
            + ", new: " + SAXProcessor.timeToString(start2.getTime(), end2.getTime()) + "\n");

  }

}
