package edu.hawaii.jmotif.logic;

/**
 * Implements an interval class. Zero based, all-inclusive end.
 * 
 * @author psenin
 * 
 */
public class Interval {

  private int start;
  private int end;
  private double coverage;

  /**
   * Constructor.
   * 
   * @param start
   * @param end
   * @param coverage
   */
  public Interval(int start, int end, double coverage) {
    this.start = start;
    this.end = end;
    this.coverage = coverage;
  }

  public double getCoverage() {
    return coverage;
  }

  public void setCoverage(double coverage) {
    this.coverage = coverage;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int getStart() {
    return this.start;
  }

  public int getEnd() {
    return this.end;
  }

  public int getLength() {
    return Math.abs(this.end - this.start);
  }

  /**
   * True if intervals overlap even by a single point.
   * 
   * @param intervalB the other interval.
   * 
   * @return true if overlap exists.
   */
  public boolean overlaps(Interval intervalB) {
    if ((this.start <= intervalB.getEnd()) && (this.end >= intervalB.getStart())) {
      return true;
    }
    return false;
  }

  /**
   * Computes the overlap in percents
   * 
   * @param otherInterval
   * @return
   */
  public Double overlapInPercent(Interval otherInterval) {
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      return Double.valueOf((Integer.valueOf(overlapEnd).doubleValue() - Integer.valueOf(
          overlapStart).doubleValue())
          / Integer.valueOf(Math.abs(this.end - this.start)).doubleValue());
    }
    return 0D;
  }

  /**
   * Counts points within an interval.
   * 
   * @param otherInterval
   * @return
   */
  public int pointsInsideOverlap(Interval otherInterval) {
    int res = 0;
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      res = Math.abs(overlapEnd - overlapStart);
    }
    return res;
  }

  /**
   * Counts points outside of interval.
   * 
   * @param otherInterval
   * @return
   */
  public int pointsOutsideOverlap(Interval otherInterval) {
    int res = 0;
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      res = res + Math.abs(overlapStart - this.start)
          + Math.abs(overlapStart - otherInterval.start);
      res = res + Math.abs(overlapEnd - this.end) + Math.abs(overlapEnd - otherInterval.end);
    }
    return res;
  }

  /**
   * How much does this extends to the left?
   * 
   * @param other
   * @return
   */
  public int extendsLeft(Interval other) {
    return other.start - this.start;
  }

  /**
   * How much this extends to the right?
   * 
   * @param other
   * @return
   */
  public int extendsRight(Interval other) {
    return this.end - other.end;
  }
}
