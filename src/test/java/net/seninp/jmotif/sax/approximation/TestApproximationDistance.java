package net.seninp.jmotif.sax.approximation;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;

public class TestApproximationDistance {

  private static final double[] testTS = { -1., -2., -1., 0., 2., 1., 1., 0. };
  private static SAXProcessor sp;

  @Before
  public void setUp() throws Exception {
    sp = new SAXProcessor();
  }

  @Test
  public void test() {
    try {
      assertEquals(2, Double.valueOf(sp.approximationDistance(Arrays.copyOfRange(testTS, 0, 4), 4,
          1, 3, NumerosityReductionStrategy.NONE, 0.01)).intValue(), 0.01);
      assertEquals(2, Double.valueOf(sp.approximationDistance(Arrays.copyOfRange(testTS, 4, 8), 4,
          1, 3, NumerosityReductionStrategy.NONE, 0.01)).intValue(), 0.01);
    }
    catch (Exception e) {
      fail("shall not throw an error");
    }
  }

}
