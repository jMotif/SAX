package edu.hawaii.jmotif.logic;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestInterval {

  private static final int startA = 15;
  private static final int endA = 77;
  private static final int coverageA = 11;
  private Interval intA;

  private static final int startB = 69;
  private static final int endB = 88;
  private static final int coverageB = 3;
  private Interval intB;

  private double delta = 0.000001D;

  @Before
  public void setUp() throws Exception {
    intA = new Interval(startA, endA, coverageA);
    intB = new Interval(startB, endB, coverageB);
  }

  @Test
  public void testInterval() {
    assertNotNull("Testing constructor.", intA);
    assertNotNull("Testing constructor.", intB);
  }

  @Test
  public void testGetCoverage() {
    assertEquals("Testing coverage", Integer.valueOf(coverageA).doubleValue(), intA.getCoverage(),
        delta);
    assertEquals("Testing coverage", Integer.valueOf(coverageB).doubleValue(), intB.getCoverage(),
        delta);
  }

  @Test
  public void testGetLength() {
    assertEquals("Testing length", Integer.valueOf(coverageA).doubleValue(), intA.getCoverage(),
        delta);
    assertEquals("Testing length", Integer.valueOf(coverageB).doubleValue(), intB.getCoverage(),
        delta);
  }

  @Test
  public void testOverlaps() {
    assertTrue("Testing for overlap", intA.overlaps(intB));
    assertTrue("Testing for overlap", intB.overlaps(intA));
  }

  @Test
  public void testOverlapInPercent() {
    assertEquals("Testing for overlap", (77. - 69) / intA.getLength(), intA.overlapInPercent(intB),
        delta);
    assertEquals("Testing for overlap", (77. - 69) / intB.getLength(), intB.overlapInPercent(intA),
        delta);
  }

  @Test
  public void testPointsInsideOverlap() {
    assertEquals("Testing for overlap", (77 - 69), intA.pointsInsideOverlap(intB));
    assertEquals("Testing for overlap", (77 - 69), intB.pointsInsideOverlap(intA));
  }

  @Test
  public void testPointsOutsideOverlap() {
    assertEquals("Testing for overlap", (intA.getLength() - (77 - 69))
        + (intB.getLength() - (77 - 69)), intA.pointsOutsideOverlap(intB));
  }

}
