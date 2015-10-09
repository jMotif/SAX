package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestNRStrategy {

  private static final NumerosityReductionStrategy none = NumerosityReductionStrategy.NONE;
  private static final NumerosityReductionStrategy exact = NumerosityReductionStrategy.EXACT;
  private static final NumerosityReductionStrategy mindist = NumerosityReductionStrategy.MINDIST;

  @Test
  public void testNRStrategy() {

    assertEquals(0, none.index());
    assertEquals(1, exact.index());
    assertEquals(2, mindist.index());

    assertEquals(NumerosityReductionStrategy.fromValue(0), none);
    assertEquals(NumerosityReductionStrategy.fromValue(1), exact);
    assertEquals(NumerosityReductionStrategy.fromValue(2), mindist);
    try {
      assertEquals(NumerosityReductionStrategy.fromValue(77), mindist);
      fail("should throw an exception");
    }
    catch (Throwable t) {
      assert true;
    }

    assertEquals(NumerosityReductionStrategy.fromString("none"), none);
    assertEquals(NumerosityReductionStrategy.fromString("exact"), exact);
    assertEquals(NumerosityReductionStrategy.fromString("mindist"), mindist);
    try {
      assertEquals(NumerosityReductionStrategy.fromString("oops"), mindist);
      fail("should throw an exception");
    }
    catch (Throwable t) {
      assert true;
    }

    assertTrue("NONE".equalsIgnoreCase(none.toString()));
    assertTrue("EXACT".equalsIgnoreCase(exact.toString()));
    assertTrue("MINDIST".equalsIgnoreCase(mindist.toString()));

  }

}
