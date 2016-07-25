package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
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
public class TestShingling {

  private static final String ts1File = "src/resources/test-data/timeseries01.csv";
  private static final int length = 15;

  /**
   * Testing the permutation production.
   */
  @Test
  public void testPermutations() throws NumberFormatException, IOException, SAXException {

    String[] arr = { "a", "b", "c" };

    String[] perm2 = SAXProcessor.getAllPermutations(arr, 2);
    assertEquals("Testing the resulting array's length.", 9, perm2.length);

    String asString2 = Arrays.toString(perm2);
    assertTrue("Testing the specific word is present.", asString2.contains("ca"));

    String[] perm5 = SAXProcessor.getAllPermutations(arr, 5);
    assertEquals("Testing the resulting array's length.", 3 * 3 * 3 * 3 * 3, perm5.length);

    String asString5 = Arrays.toString(perm5);
    assertTrue("Testing the specific word is present.", asString5.contains("caaca"));

  }

  /**
   * Testing the permutation production.
   */
  @Test
  public void testShingling() throws NumberFormatException, IOException, SAXException {

    final SAXProcessor sp = new SAXProcessor();
    final Alphabet a = new NormalAlphabet();

    final double[] ts1 = TSProcessor.readFileColumn(ts1File, 0, length);

    SAXRecords sax = sp.ts2saxViaWindow(ts1, 3, 3, a.getCuts(3), NumerosityReductionStrategy.NONE,
        0.001);

    Map<String, Integer> shingles = sp.ts2Shingles(ts1, 3, 3, 3, NumerosityReductionStrategy.NONE,
        0.001, 3);

    for (Entry<String, Integer> shinglesEntry : shingles.entrySet()) {
      SAXRecord saxEntry = sax.getByWord(shinglesEntry.getKey());
      if (null != saxEntry) {
        assertEquals("testing shingling", Integer.valueOf(saxEntry.getIndexes().size()),
            shinglesEntry.getValue());
      }
      else {
        assertEquals("testing shingling", Integer.valueOf(0), shinglesEntry.getValue());
      }
    }
  }

}
