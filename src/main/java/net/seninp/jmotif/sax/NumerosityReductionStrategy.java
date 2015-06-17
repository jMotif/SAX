package net.seninp.jmotif.sax;

/**
 * The SAX Collection srategy.
 * 
 * @author Pavel Senin
 * 
 */
public enum NumerosityReductionStrategy {

  /** No reduction at all - all the words going make it into collection. */
  NONE(0),

  /** Exact - the strategy based on the exact string match. */
  EXACT(1),

  /** Classic - the Lin's and Keogh's MINDIST based strategy. */
  MINDIST(2);

  private final int index;

  /**
   * Constructor.
   * 
   * @param index The strategy index.
   */
  NumerosityReductionStrategy(int index) {
    this.index = index;
  }

  /**
   * Gets the integer index of the instance.
   * 
   * @return integer key of the instance.
   */
  public int index() {
    return index;
  }

  /**
   * Makes a strategy out of integer. 0 stands for NONE, 1 for EXACT, and 3 for MINDIST.
   * 
   * @param value the key value.
   * @return the new Strategy instance.
   */
  public static NumerosityReductionStrategy fromValue(int value) {
    switch (value) {
    case 0:
      return NumerosityReductionStrategy.NONE;
    case 1:
      return NumerosityReductionStrategy.EXACT;
    case 2:
      return NumerosityReductionStrategy.MINDIST;
    default:
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    switch (this.index) {
    case 0:
      return "NONE";
    case 1:
      return "EXACT";
    case 2:
      return "MINDIST";
    default:
      throw new RuntimeException("Unknown index:" + this.index);
    }
  }

  /**
   * Parse the string value into an instance.
   * 
   * @param value the string value.
   * @return new instance.
   */
  public static NumerosityReductionStrategy fromString(String value) {
    if ("none".equalsIgnoreCase(value)) {
      return NumerosityReductionStrategy.NONE;
    }
    else if ("exact".equalsIgnoreCase(value)) {
      return NumerosityReductionStrategy.EXACT;
    }
    else if ("mindist".equalsIgnoreCase(value)) {
      return NumerosityReductionStrategy.MINDIST;
    }
    else {
      throw new RuntimeException("Unknown index:" + value);
    }
  }
}
