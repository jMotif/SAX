package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class TestApproxDistance {

  private static final double[] series = { 1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13.,
      14., 15. };

  private SAXProcessor sp;
  private TSProcessor tp;

  @Before
  public void setUp() throws Exception {
    sp = new SAXProcessor();
  }

  @Test
  public void test() {
    double dist, distZnorm;
    try {

      dist = sp.approximationDistance(series, 15, 7, 5.0);
      assertEquals("testing approx distance", 8.0, dist, 0.000001);

      distZnorm = sp.approximationDistance(series, 15, 7, 0.01);
      assertEquals("testing approx distance", 1.788854, distZnorm, 0.000001);

      double newApproximationDistance = sp.approximationDistance(series, 15, 7, 3, 0.01);
      assertEquals("testing approx distance", 4.47213595499958, newApproximationDistance, 0.01);
    }
    catch (Exception e) {
      fail("exception shall not be thrown!");
    }
  }

}
