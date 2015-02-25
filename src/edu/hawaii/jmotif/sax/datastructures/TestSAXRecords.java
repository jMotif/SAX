package edu.hawaii.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXProcessor;
import edu.hawaii.jmotif.sax.TSProcessor;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;

public class TestSAXRecords {

  private static final String filenameTEK14 = "test/data/TEK14.txt";

  private double[] ts1;

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testProperIndexing() throws Exception {

    ts1 = TSProcessor.readFileColumn(filenameTEK14, 0, 0);

    NormalAlphabet na = new NormalAlphabet();

    SAXProcessor sp = new SAXProcessor();
    SAXRecords parallelRes = sp.ts2saxViaWindow(ts1, 400, 6, na.getCuts(3),
        NumerosityReductionStrategy.EXACT, 0.01);

    String str = parallelRes.getSAXString(" ");

    parallelRes.buildIndex();

    String str1 = "";

    for (int i = 11; i < 47; i++) {
      SaxRecord r = parallelRes.getByIndex(parallelRes.mapStringIndexToTSPosition(i));
      str1 = str1.concat(String.valueOf(r.getPayload()) + " ");
    }

    assertTrue("Asserting substring existence", str.indexOf(str1) > 0);

    assertEquals("Asserting substring the index", 11 * 6 + 11 * 1, str.indexOf(str1));

  }

}
