package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Test SAX factory methods.
 * 
 * @author Pavel Senin
 * 
 */
public class TestSAXProcessor {

  private static final String ts1File = "src/resources/test-data/timeseries01.csv";
  private static final String ts2File = "src/resources/test-data/timeseries02.csv";
  private static final String ts3File = "src/resources/test-data/timeseries03.csv";

  private static final String ts1StrRep10 = "bcjkiheebb";
  private static final String ts2StrRep10 = "bcefgijkdb";

  private static final String ts1StrRep14 = "bcdijjhgfeecbb";
  private static final String ts2StrRep14 = "bbdeeffhijjfbb";

  private static final String ts1StrRep7 = "bcggfddba";
  private static final String ts2StrRep7 = "accdefgda";

  private static final int length = 15;
  private static final int strLength = 10;

  private static final Alphabet normalA = new NormalAlphabet();

  private static final double delta = 0.001;

  // logging stuff
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(TestSAXProcessor.class);

  /**
   * Testing the concatenated time series SAX conversion.
   * 
   * @throws NumberFormatException if error occurs.
   * @throws IOException if error occurs.
   * @throws SAXException if error occurs.
   */
  @Test
  public void testConnectedConversion() throws NumberFormatException, IOException, SAXException {

    final SAXProcessor sp = new SAXProcessor();
    final double[] ts = TSProcessor.readFileColumn(ts3File, 0, 0);

    ArrayList<Integer> skips = new ArrayList<Integer>();
    for (int i = 30 - 6; i < 30; i++) {
      skips.add(i);
    }

    SAXRecords regularSAX = sp.ts2saxViaWindow(ts, 6, 3, normalA.getCuts(3),
        NumerosityReductionStrategy.NONE, 0.01);
    LOGGER.debug("NONE: there are " + regularSAX.getAllIndices().size() + " words: \n"
        + regularSAX.getSAXString(" ") + "\n" + regularSAX.getAllIndices());
    SAXRecords saxData = sp.ts2saxViaWindowSkipping(ts, 6, 3, normalA.getCuts(3),
        NumerosityReductionStrategy.NONE, 0.01, skips);
    LOGGER.debug("NONE with skips: there are " + saxData.getAllIndices().size() + " words: \n"
        + saxData.getSAXString(" ") + "\n" + saxData.getAllIndices());

    regularSAX = sp.ts2saxViaWindow(ts, 6, 3, normalA.getCuts(3), NumerosityReductionStrategy.EXACT,
        0.01);
    assertNotNull("asserting the processing result", regularSAX);
    LOGGER.debug("EXACT: there are " + regularSAX.getAllIndices().size() + " words: \n"
        + regularSAX.getSAXString(" ") + "\n" + regularSAX.getAllIndices());
    saxData = sp.ts2saxViaWindowSkipping(ts, 6, 3, normalA.getCuts(3),
        NumerosityReductionStrategy.EXACT, 0.01, skips);
    LOGGER.debug("EXACT with skips: there are " + saxData.getAllIndices().size() + " words: \n"
        + saxData.getSAXString(" ") + "\n" + saxData.getAllIndices());

  }

  /**
   * Test the SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testTs2SAXByChunks() throws Exception {

    final SAXProcessor sp = new SAXProcessor();
    final TSProcessor tp = new TSProcessor();

    final double[] ts1 = TSProcessor.readFileColumn(ts1File, 0, length);
    final double[] ts2 = TSProcessor.readFileColumn(ts2File, 0, length);

    final double[] ser = { -1.0, -2.0, -1.0, 0.0, 2.0, 1.0, 1.0, 0.0 };

    LOGGER.debug(" ** " + Arrays.toString(tp.paa(ser, 3)));

    // series #1 goes here
    SAXRecords ts1sax = sp.ts2saxByChunking(ts1, 10, normalA.getCuts(11), delta);

    assertEquals("testing SAX", strLength, ts1sax.getSAXString("").length());
    assertTrue("testing SAX", ts1StrRep10.equalsIgnoreCase(ts1sax.getSAXString("")));
    // test positions
    // bcjkiheebb
    //
    Integer[] bPositions = ts1sax.getByWord("b").getIndexes().toArray(new Integer[3]);
    Arrays.sort(bPositions);
    assertEquals(0, bPositions[0].intValue());
    assertEquals(12, bPositions[1].intValue());
    assertEquals(13, bPositions[2].intValue());

    String ts1sax2 = sp.ts2saxByChunking(ts1, 14, normalA.getCuts(10), delta).getSAXString("");
    assertEquals("testing SAX", 14, ts1sax2.length());
    assertTrue("testing SAX", ts1StrRep14.equalsIgnoreCase(ts1sax2));

    String ts1sax3 = sp.ts2saxByChunking(ts1, 9, normalA.getCuts(7), delta).getSAXString("");
    assertEquals("testing SAX", 9, ts1sax3.length());
    assertTrue("testing SAX", ts1StrRep7.equalsIgnoreCase(ts1sax3));

    // series #2 goes here
    String ts2sax = sp.ts2saxByChunking(ts2, 10, normalA.getCuts(11), delta).getSAXString("");

    assertEquals("testing SAX", strLength, ts2sax.length());
    assertTrue("testing SAX", ts2StrRep10.equalsIgnoreCase(ts2sax));

    ts2sax = sp.ts2saxByChunking(ts2, 14, normalA.getCuts(10), delta).getSAXString("");
    assertEquals("testing SAX", 14, ts2sax.length());
    assertTrue("testing SAX", ts2StrRep14.equalsIgnoreCase(ts2sax));

    ts2sax = sp.ts2saxByChunking(ts2, 9, normalA.getCuts(7), delta).getSAXString("");
    assertEquals("testing SAX", 9, ts2sax.length());
    assertTrue("testing SAX", ts2StrRep7.equalsIgnoreCase(ts2sax));
  }

  /**
   * Test the SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testTs2SAXByGlobalChunks() throws Exception {

    final SAXProcessor sp = new SAXProcessor();
    final double[] ts1 = TSProcessor.readFileColumn(ts1File, 0, length);

    SAXRecords saxdata = sp.ts2saxViaWindowGlobalZNorm(ts1, 5, 5, normalA.getCuts(3),
        NumerosityReductionStrategy.NONE, 0.01);

    SAXRecord entry = saxdata.getByIndex(7);

    // library(jmotif)
    // library(data.table)
    // dat<-fread("../resources/test-data/timeseries01.csv")
    // zdat <- jmotif::znorm(dat$V1, 0.01)
    // win = zdat[8:12]
    // motif::series_to_chars(win, 3)
    // [1] "c" "c" "b" "b" "b"
    assertTrue("ccbbb".equalsIgnoreCase(String.valueOf(entry.getPayload())));
  }

  /**
   * Test the discretization.
   *
   * @throws Exception if error occur.
   */
  @Test
  public void testTs2sax() throws Exception {

    final TSProcessor tp = new TSProcessor();
    final SAXProcessor sp = new SAXProcessor();

    double[] ts2 = TSProcessor.readFileColumn(ts2File, 0, length);

    String ts2str_0 = sp
        .ts2saxByChunking(tp.subseriesByCopy(ts2, 0, 5), 5, normalA.getCuts(10), delta)
        .getSAXString("");
    String ts2str_3 = sp
        .ts2saxByChunking(tp.subseriesByCopy(ts2, 3, 8), 5, normalA.getCuts(10), delta)
        .getSAXString("");
    String ts2str_7 = sp
        .ts2saxByChunking(tp.subseriesByCopy(ts2, 7, 12), 5, normalA.getCuts(10), delta)
        .getSAXString("");

    SAXRecords ts2SAX = sp.ts2saxViaWindow(ts2, 5, 5, normalA.getCuts(10),
        NumerosityReductionStrategy.NONE, delta);

    assertEquals("Testing conversion", ts2.length - 5 + 1, ts2SAX.size());

    assertNotNull("Testing ts2sax", ts2SAX.getByWord(ts2str_0));
    assertNotNull("Testing ts2sax", ts2SAX.getByWord(ts2str_3));
    assertNotNull("Testing ts2sax", ts2SAX.getByWord(ts2str_7));

    assertEquals("Testing ts2sax", ts2SAX.getByWord(ts2str_0).getIndexes().iterator().next(),
        new Integer(0));
    assertEquals("Testing ts2sax", ts2SAX.getByWord(ts2str_3).getIndexes().iterator().next(),
        new Integer(3));
    assertEquals("Testing ts2sax", ts2SAX.getByWord(ts2str_7).getIndexes().iterator().next(),
        new Integer(7));

    SAXRecords ts2SAXerror = null;

    try {
      ts2SAXerror = sp.ts2saxViaWindow(ts2, ts2.length + 1, 5, normalA.getCuts(10),
          NumerosityReductionStrategy.NONE, delta);
      fail("Exception must be thrown!");
    }
    catch (SAXException e) {
      assertNull(ts2SAXerror);
    }
  }

  /**
   * Test the MINDIST distance.
   */
  @Test
  public void testMINDIST() {

    final double a3distValue = 0.861455;
    final double refDist = Math.sqrt(128.0 / 8.0)
        * Math.sqrt(a3distValue * a3distValue + a3distValue * a3distValue);
    final String a = "baabccbc";
    final String b = "babcacca";

    final SAXProcessor sp = new SAXProcessor();

    // try the normal operation
    try {
      assertEquals(
          sp.saxMinDist(a.toCharArray(), b.toCharArray(), normalA.getDistanceMatrix(3), 128, 8),
          refDist, delta);
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }

    // try the abnormal operation -- not letter is in the word
    try {
      assertEquals(sp.saxMinDist("baabcc4c".toCharArray(), b.toCharArray(),
          normalA.getDistanceMatrix(2), 128, 8), refDist, delta);
      fail("exception not thrown!");
    }
    catch (SAXException e) {
      assert true;
    }

    // try the abnormal operation -- length is not equal
    try {
      assertEquals(sp.saxMinDist("baabccc".toCharArray(), b.toCharArray(),
          normalA.getDistanceMatrix(2), 128, 8), refDist, delta);
      fail("exception not thrown!");
    }
    catch (SAXException e) {
      assert true;
    }

    // try the abnormal operation -- "c" is not in the alphabet
    try {
      assertEquals(
          sp.saxMinDist(a.toCharArray(), b.toCharArray(), normalA.getDistanceMatrix(2), 128, 8),
          refDist, delta);
      fail("exception not thrown!");
    }
    catch (SAXException e) {
      assert true;
    }

    assertFalse(sp.checkMinDistIsZero(a.toCharArray(), b.toCharArray()));
    assertTrue(sp.checkMinDistIsZero("aabbccdd".toCharArray(), "bbccddee".toCharArray()));

  }

  /**
   * Test to string conversion.
   *
   */
  @Test
  public void testTs2String() {
    final double[] series = { -1., -2., -1., 0., 2., 1., 1., 0. };
    final SAXProcessor sp = new SAXProcessor();
    try {
      assertTrue(String.valueOf(sp.ts2string(series, 3, normalA.getCuts(3), 0.001)).equals("acc"));
      assertTrue(
          String.valueOf(sp.ts2string(series, 8, normalA.getCuts(3), 0.001)).equals("aaabcccb"));
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }
  }

  /**
   * Test char distance.
   *
   */
  @Test
  public void testCharDistance() {
    final SAXProcessor sp = new SAXProcessor();
    assertEquals(sp.charDistance('a', 'a'), 0);
    assertEquals(sp.charDistance('a', 'c'), 2);
    assertEquals(sp.charDistance('a', 'e'), 4);
  }

  /**
   * Test str distance.
   *
   */
  @Test
  public void testStrDistance() {

    final SAXProcessor sp = new SAXProcessor();

    try {
      assertEquals(sp.strDistance("aaa".toCharArray(), "aaa".toCharArray()), 0);
      assertEquals(sp.strDistance("aaa".toCharArray(), "aac".toCharArray()), 2);
      assertEquals(sp.strDistance("aaa".toCharArray(), "abc".toCharArray()), 3);
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }

    try {
      assertEquals(sp.strDistance("aaa".toCharArray(), "aaaa".toCharArray()), 0);
      fail("exception shall be thrown!");
    }
    catch (SAXException e) {
      assert true;
    }

  }

}
