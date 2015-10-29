package net.seninp.jmotif.issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Testing Issue #11, last sliding window position.
 * 
 * @author psenin
 *
 */
public class TestIssue11 {

  private static final double[] series30 = { -0.4075238, -1.3902413, -1.1378248, 0.4693141,
      0.1439972, 0.2189808, -0.2295596, 0.7483967, 0.3947809, -1.1495477, -1.3420485, 2.7371262,
      -0.6455657, 0.1358455, 1.8835421, 2.4793993, 0.4210423, 0.4750554, -0.7061231, -0.4705009,
      0.1005799, -0.8561212, -1.1428216, 2.0228181, 0.5899366, 1.0976786, 0.1651252, 0.9216971,
      -0.3341624, -0.2496580 };

  private static final int windowSize = 4;
  private static final int paaSize = 2;
  private static final int alphabetSize = 3;
  private static final NumerosityReductionStrategy nrStrategy = NumerosityReductionStrategy.NONE;
  private static final double normThreshold = 0.001;

  SAXProcessor sp;
  NormalAlphabet na;

  @Before
  public void setUp() throws Exception {
    sp = new SAXProcessor();
    na = new NormalAlphabet();
  }

  @Test
  public void test() {
    try {
      SAXRecords saxTransform = sp.ts2saxViaWindow(series30, windowSize, paaSize,
          na.getCuts(alphabetSize), nrStrategy, normThreshold);
      assertEquals(27, saxTransform.getAllIndices().size());
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }
  }

}
