package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

/**
 * Test the tsp.
 * 
 * @author Pavel Senin.
 * 
 * 
 * 
 * 
 */
public class TestTSProcessor {

  private static final String ts1File = "src/resources/test-data/timeseries01.csv";
  private static final String ts2File = "src/resources/test-data/timeseries02.csv";

  private static final String ts1NormFile = "src/resources/test-data/timeseries01.norm.csv";
  private static final String ts2NormFile = "src/resources/test-data/timeseries02.norm.csv";

  private static final String ts1PAAFile = "src/resources/test-data/timeseries01.PAA10.csv";
  private static final String ts2PAAFile = "src/resources/test-data/timeseries02.PAA10.csv";

  private static final int length = 15;
  private static final int PAAlength = 10;
  private static final double delta = 0.000001;

  private static final double ts1Max = 9.2;
  private static final double ts1Min = 1.34;
  private static final double ts2Max = 8.83;
  private static final double ts2Min = 0.5;

  private static final Alphabet normalA = new NormalAlphabet();

  TSProcessor tsp;
  private double[] ts1;
  private double[] ts2;

  /**
   * Test set-up.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    tsp = new TSProcessor();
    ts1 = tsp.readTS(ts1File, length);
    ts2 = tsp.readTS(ts2File, length);
  }

  /**
   * Test the extremum calculations.
   */
  @Test
  public void testExtremum() {
    assertEquals("max", ts1Max, tsp.max(ts1), delta);
    assertEquals("max", ts2Max, tsp.max(ts2), delta);
    assertEquals("min", ts1Min, tsp.min(ts1), delta);
    assertEquals("min", ts2Min, tsp.min(ts2), delta);
    //
    // now set the value of the second element as NaN
    ts1[1] = Double.NaN;
    ts2[2] = Double.NaN;
    assertEquals("max", ts1Max, tsp.max(ts1), delta);
    assertEquals("max", ts2Max, tsp.max(ts2), delta);
    assertEquals("min", ts1Min, tsp.min(ts1), delta);
    assertEquals("min", ts2Min, tsp.min(ts2), delta);
  }

  /**
   * Test the median calculation.
   */
  @Test
  public void testMedian() {
    assertEquals("testing the mean", 3.85, tsp.median(ts1), delta);
    assertEquals("testing the mean", 3.83, tsp.median(ts2), delta);

    final double[] badArray = {};
    assertTrue("testing the mean", Double.isNaN(tsp.mean(badArray)));
  }

  /**
   * Test the mean calculation.
   */
  @Test
  public void testMean() {
    assertEquals("testing the mean", 4.606667, tsp.mean(ts1), delta);
    assertEquals("testing the mean", 4.01, tsp.mean(ts2), delta);

    final double[] badArray = {};
    assertTrue("testing the mean", Double.isNaN(tsp.mean(badArray)));
  }

  /**
   * Test the variance calculation.
   */
  @Test
  public void testVar() {
    assertEquals("variance", 6.971267, tsp.var(ts1), delta);
    assertEquals("variance", 7.409971, tsp.var(ts2), delta);
  }

  /**
   * Test the standard deviation calculation.
   */
  @Test
  public void testStdev() {
    assertEquals("stdev", 2.640316, tsp.stDev(ts1), delta);
  }

  /**
   * Test the normalize routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNormalize() throws Exception {

    // read the normalized data
    double[] ts1Norm = tsp.readTS(ts1NormFile, length);
    double[] ts2Norm = tsp.readTS(ts2NormFile, length);

    // get the normal data through the code
    double[] ts1NormTest = tsp.znorm(ts1, 0.001);
    double[] ts2NormTest = tsp.znorm(ts2, 0.001);

    for (int i = 0; i < ts1Norm.length; i++) {
      assertEquals("normalization", ts1Norm[i], ts1NormTest[i], delta);
    }

    for (int i = 0; i < ts2Norm.length; i++) {
      assertEquals("normalization", ts2Norm[i], ts2NormTest[i], delta);
    }

    // get the norm 1 data
    double[] ts1normOne = tsp.normOne(ts1);
    double[] ts2normOne = tsp.normOne(ts2);

    boolean seenOne = false;
    for (int i = 0; i < ts1normOne.length; i++) {
      assertTrue(ts1normOne[i] <= 1);
      if (ts1normOne[i] == 1.0) {
        seenOne = true;
      }
    }
    assertTrue(seenOne);

    seenOne = false;
    for (int i = 0; i < ts2normOne.length; i++) {
      assertTrue(ts2normOne[i] <= 1);
      if (ts2normOne[i] == 1.0) {
        seenOne = true;
      }
    }
    assertTrue(seenOne);
  }

  /**
   * Test the PAA routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testPAA() throws Exception {
    // read the normalized data
    double[] ts1Norm = tsp.readTS(ts1NormFile, length);
    double[] ts2Norm = tsp.readTS(ts2NormFile, length);

    // read the PAA data
    double[] ts1PAA10 = tsp.readTS(ts1PAAFile, PAAlength);
    double[] ts2PAA10 = tsp.readTS(ts2PAAFile, PAAlength);

    // get the normal data through the code
    double[] ts1PAATest = tsp.paa(ts1Norm, PAAlength);
    double[] ts2PAATest = tsp.paa(ts2Norm, PAAlength);

    for (int i = 0; i < ts1PAA10.length; i++) {
      assertEquals("PAA", ts1PAA10[i], ts1PAATest[i], delta);
    }

    for (int i = 0; i < ts2PAA10.length; i++) {
      assertEquals("PAA", ts2PAA10[i], ts2PAATest[i], delta);
    }
  }

  /**
   * Test the SAX conversion.
   * 
   * @throws SAXException if error occurs.
   * 
   */
  @Test
  public void testNum2Char() throws SAXException {
    // private static final double[] case2 = { 0 };
    assertEquals("test num2char", 'a', tsp.num2char(-0.5, normalA.getCuts(2)));
    assertEquals("test num2char", 'b', tsp.num2char(0.5, normalA.getCuts(2)));
    assertEquals("test num2char", 'b', tsp.num2char(0.0, normalA.getCuts(2)));
    double[] ts0 = { -0.5, 0.5, 0.0 };
    assertTrue("test num2char",
        "abb".equalsIgnoreCase(new String(tsp.ts2String(ts0, normalA.getCuts(2)))));
    // private static final double[] case7 = { -1.07, -0.57, -0.18, 0.18, 0.57, 1.07 };
    assertEquals("test num2char", 'd', tsp.num2char(-0.179, normalA.getCuts(7)));
    assertEquals("test num2char", 'd', tsp.num2char(-0.18, normalA.getCuts(7)));
    assertEquals("test num2char", 'c', tsp.num2char(-0.1801, normalA.getCuts(7)));
    double[] ts1 = { -0.179, -0.18, -0.1801 };
    assertTrue("test num2char",
        "ddc".equalsIgnoreCase(new String(tsp.ts2String(ts1, normalA.getCuts(7)))));

  }

  /**
   * Test the SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testTS2Index() throws Exception {
    // read the PAA data
    double[] ts1PAA10 = tsp.readTS(ts1PAAFile, PAAlength);

    int[] idx1 = tsp.ts2Index(ts1PAA10, normalA, 10);

    assertEquals("Testing ts2index", Integer.valueOf(idx1[1]), Integer.valueOf(2));
    assertEquals("Testing ts2index", Integer.valueOf(idx1[3]), Integer.valueOf(9));
    assertEquals("Testing ts2index", Integer.valueOf(idx1[7]), Integer.valueOf(4));
  }

  /**
   * Test series2string.
   * 
   */
  @Test
  public void series2String() {

    final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    final DecimalFormat df = (DecimalFormat) nf;
    df.applyPattern("#.0");

    final double[] formattedTs = { -1.07, -0.57, -0.18, 0.18, 0.57, 1.07 };

    String seriesAsString = tsp.seriesToString(formattedTs, df);

    assertTrue(seriesAsString.contains("-1.1"));
    assertTrue(seriesAsString.contains(".6"));
    assertTrue(seriesAsString.contains("-.6"));

  }

}
