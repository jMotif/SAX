package net.seninp.jmotif.sax;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestApproxDistance {

  private static final double[] series = { 1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13.,
      14., 15. };

  private SAXProcessor sp;

  @Before
  public void setUp() throws Exception {
    sp = new SAXProcessor();
  }

  @Test
  public void test() {
    double dist;
    try {
      dist = sp.approximationDistance(series, 15, 7, 5.0);
      assertEquals("testing approx distance", 8.0, dist, 0.000001);
    }
    catch (Exception e) {
      fail("exception shall not be thrown!");
    }
  }

}
