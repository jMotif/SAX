package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import org.junit.Test;

/**
 * Testing SAX records store.
 * 
 * @author psenin
 * 
 */
public class TestSAXRecords {

  private static final String filenameTEK14 = "src/resources/test-data/TEK14.txt";

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testProperIndexing() throws Exception {
    double[] ts1 = TSProcessor.readFileColumn(filenameTEK14, 0, 0);
    NormalAlphabet na = new NormalAlphabet();
    SAXProcessor sp = new SAXProcessor();
    SAXRecords res = sp.ts2saxViaWindow(ts1, 400, 6, na.getCuts(3),
        NumerosityReductionStrategy.EXACT, 0.01);
    String str = res.getSAXString(" ");
    res.buildIndex();
    String str1 = "";
    for (int i = 11; i < 47; i++) {
      SAXRecord r = res.getByIndex(res.mapStringIndexToTSPosition(i));
      str1 = str1.concat(String.valueOf(r.getPayload()) + " ");
    }
    assertTrue("Asserting substring existence", str.indexOf(str1) > 0);
    assertEquals("Asserting substring the index", 11 * 6 + 11 * 1, str.indexOf(str1));
  }

  /**
   * Test the proper indexing in NONE strategy.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNoneIndexing() throws Exception {
    int slidinWindowSize = 100;
    double[] ts1 = TSProcessor.readFileColumn(filenameTEK14, 0, 0);
    NormalAlphabet na = new NormalAlphabet();
    SAXProcessor sp = new SAXProcessor();
    SAXRecords res = sp.ts2saxViaWindow(ts1, slidinWindowSize, 4, na.getCuts(3),
        NumerosityReductionStrategy.NONE, 0.01);
    //
    // there should be a record index at each point except ts length - sliding window
    //
    for (int i = 0; i < ts1.length - slidinWindowSize; i++) {
      assertNotNull("Asserting the proper dicretization.", res.getByIndex(i));
    }
    assertNull("Asserting the proper dicretization termination.",
        res.getByIndex(ts1.length - slidinWindowSize + 1));
    //
    // test exclude positions
    //
    int excludedLen = 15;
    int excludedStart = 17;
    ArrayList<Integer> excluded = new ArrayList<Integer>(excludedLen);
    for (int i = 0; i < excludedLen; i++) {
      excluded.add(i + excludedStart);
    }
    res.excludePositions(excluded);
    assertNotNull("Asserting the proper dicretization.", res.getByIndex(excludedStart - 1));
    for (int i = 0; i < excludedLen; i++) {
      assertNull("Asserting the proper dicretization.", res.getByIndex(i + excludedStart));
    }
    assertNotNull("Asserting the proper dicretization.",
        res.getByIndex(excludedStart + excludedLen + 1));
    //
    // test drop by index
    //
    SAXRecord rec = res.getByIndex(res.getIndexes().iterator().next());
    String str = String.valueOf(rec.getPayload());
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    indexes.addAll(rec.getIndexes());
    for (Integer i : indexes) {
      res.dropByIndex(i);
    }
    assertNull("Assert drop by index.", res.getByWord(str));
    //
    // test add all
    //
    SAXRecords records = new SAXRecords();
    for (Integer i : indexes) {
      records.add(str.toCharArray(), i);
    }
    res.addAll(records);
    rec = res.getByWord(str);
    assertEquals("Asserting record indexes length", rec.getIndexes().size(), indexes.size());
  }
}
