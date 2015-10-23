package net.seninp.jmotif.distance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the Euclidean distance.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestEuclideanDistance {

  private EuclideanDistance ed;

  // 1D points for the test
  private static final double[] testPoint1D1 = { 0.545 };
  private static final double[] testPoint1D2 = { 0.845 };

  // 3D points for the test
  private static final double[] testPoint3D1 = { 0.545, 0.856, 0.856 };
  private static final double[] testPoint3D2 = { 0.845, 0.654, 0.986 };

  private static final int[] testPoint3D1I = { 545, 856, 856 };
  private static final int[] testPoint3D2I = { 845, 654, 986 };

  // 2d series for the test
  private static final double[][] testSeries1 = { { 0.2, 0.6 }, { 0.3, 0.5 }, { 0.4, 0.4 },
      { 0.5, 0.3 }, { 0.6, 0.2 } };

  private static final double[][] testSeries2 = { { 1.0, 1.8 }, { 1.2, 1.6 }, { 1.4, 1.4 },
      { 1.6, 1.2 }, { 1.8, 1.0 } };

  private static final double[][] testSeries3 = { { 1.0, 1.8 }, { 1.2, 1.6 }, { 1.4, 1.4 },
      { 1.6, 1.2 } };

  private static final double DELTA = 0.000001D;

  @Before
  public void setUp() {
    ed = new EuclideanDistance();
  }

  /**
   * Test the distance between single points.
   * 
   */
  @Test
  public void testPointDistance() {

    try {

      // test the distance between points: distance(double p1, double p2)
      //
      double dd = Math.abs(testPoint1D2[0] - testPoint1D1[0]);
      assertEquals("test 1D distance", dd, ed.distance(testPoint1D1, testPoint1D2), DELTA);
      assertEquals("test 1D distance", dd * dd, ed.distance2(testPoint1D1, testPoint1D2), DELTA);

      assertEquals("test 1D distance", dd, ed.distance(testPoint1D1[0], testPoint1D2[0]), DELTA);
      assertEquals("test 1D distance", dd * dd, ed.distance2(testPoint1D1[0], testPoint1D2[0]),
          DELTA);

      // test the distance between points: distance(int p1, int p2)
      //
      int di = Math.abs(Double.valueOf(testPoint1D1[0] * 10.0).intValue()
          - Double.valueOf(testPoint1D2[0] * 10.0).intValue());
      assertEquals("test 1D distance", (double) di,
          ed.distance(Double.valueOf(testPoint1D1[0] * 10.0).intValue(),
              Double.valueOf(testPoint1D2[0] * 10.0).intValue()),
          DELTA);

      // test the distance between points: distance(double p1, double p2)
      //
      assertEquals("test point distance", 0.3843228, ed.distance(testPoint3D1, testPoint3D2),
          DELTA);
      assertEquals("test point distance", 0.147704, ed.distance2(testPoint3D1, testPoint3D2),
          DELTA);

      // test the distance between points: distance(int p1, int p2)
      //
      assertEquals("test point distance", 384.32278100, ed.distance(testPoint3D1I, testPoint3D2I),
          DELTA);

      // test the normalized distance
      //
      assertEquals("test point distance", 0.3843228 / 3.0,
          ed.normalizedDistance(testPoint3D1, testPoint3D2), DELTA);

    }
    catch (Exception e) {
      fail("Shouldn't throw any exception");
    }

    // test for exceptions
    //
    try {
      assertEquals("test point distance", 0.3843228, ed.distance(testPoint1D1, testPoint3D2),
          DELTA);
      fail("Exception is not thrown!");
    }
    catch (Exception e) {
      assert true;
    }

    try {
      int[] badSeries = { 15, 16 };
      assertEquals("test point distance", 147704.0, ed.distance2(testPoint3D1I, badSeries), DELTA);
      fail("Exception is not thrown!");
    }
    catch (Exception e) {
      assert true;
    }

  }

  /**
   * Test the distance between two series.
   * 
   */
  @Test
  public void testSeriesDistance() {

    // test the normal case
    //
    try {
      Double dist = ed.seriesDistance(testSeries1, testSeries2);
      assertEquals("testing distance, ", 3.193743885, dist, DELTA);
    }
    catch (Exception e) {
      fail("Should not throw any exception here.");
    }

    // test for exception
    //
    try {
      @SuppressWarnings("unused")
      Double dist = ed.seriesDistance(testSeries1, testSeries3);
      fail("Should throw exception here.");
    }
    catch (Exception e) {
      assert true;
    }

    // test with integers
    //
    int[] x = { 0, 0, 1, 1, 1, 1 };
    int[] y = { 1, 0, 1, 1, 0, 1 };
    try {
      assertEquals(1.414214, ed.distance(x, y), 0.001);
    }
    catch (Exception e) {
      fail("should throw an exception");
    }

  }

}
