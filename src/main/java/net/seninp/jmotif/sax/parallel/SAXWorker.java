package net.seninp.jmotif.sax.parallel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

/**
 * A callable worker class that will translate a given interval of the timeseries into SAX.
 *
 * @author psenin
 */
public class SAXWorker implements Callable<HashMap<Integer, char[]>> {

  /**
   * The worker ID.
   */
  private final long id;

  /**
   * The input timeseries.
   */
  private final double[] ts;

  /**
   * The conversion start index.
   */
  private final int intervalStart;

  /**
   * The conversion end index.
   */
  private final int intervalEnd;

  /**
   * The SAX discretization sliding window parameter size.
   */
  private final int saxWindowSize;

  /**
   * The SAX discretization PAA size.
   */
  private final int saxPAASize;

  /**
   * The SAX discretization alphabet size.
   */
  private final int saxAlphabetSize;

  /**
   * The SAX discretization sliding window parameter size.
   */
  private final NumerosityReductionStrategy numerosityReductionStrategy;

  /**
   * The SAX discretization normalization threshold.
   */
  private final double normalizationThreshold;

  // logging stuff
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(SAXWorker.class);

  /**
   * Performs SAX discretization.
   *
   * @param id the job id.
   * @param data the data array.
   * @param intervalStart from which coordinate to start the conversion.
   * @param intervalEnd where to end conversion (non-ionclusive).
   * @param windowSize SAX window size.
   * @param paaSize SAX paa size.
   * @param alphabetSize SAX alphabet size.
   * @param nrs The numerosity reduction strategy.
   * @param normalizationThreshold The normalization strategy.
   */
  public SAXWorker(long id, double[] data, int intervalStart, int intervalEnd, int windowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy nrs,
      double normalizationThreshold) {
    super();
    this.id = id;
    this.ts = data;
    this.intervalStart = intervalStart;
    this.intervalEnd = intervalEnd;
    this.saxWindowSize = windowSize;
    this.saxPAASize = paaSize;
    this.saxAlphabetSize = alphabetSize;
    this.numerosityReductionStrategy = nrs;
    this.normalizationThreshold = normalizationThreshold;
    LOGGER.debug(
        "sax worker instance id {}, data {}, window {}, paa  {}, alphabet {}, nr {}, threshold: {}, start: {}, end: {}",
        this.id, this.ts.length, this.saxWindowSize, this.saxPAASize, this.saxAlphabetSize,
        this.numerosityReductionStrategy, normalizationThreshold, this.intervalStart,
        this.intervalEnd);

  }

  @Override
  public HashMap<Integer, char[]> call() throws Exception {

    NormalAlphabet na = new NormalAlphabet();
    TSProcessor tsp = new TSProcessor();
    SAXProcessor sp = new SAXProcessor();

    HashMap<Integer, char[]> res = new HashMap<Integer, char[]>();
    res.put(-1, String.valueOf(this.id).toCharArray());

    // scan across the time series extract sub sequences, and convert
    // them to strings
    char[] previousString = null;
    for (int i = this.intervalStart; i < this.intervalEnd - (this.saxWindowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(this.ts, i, i + this.saxWindowSize);

      // Z normalize it
      subSection = tsp.znorm(subSection, normalizationThreshold);

      // perform PAA conversion if needed
      double[] paa = tsp.paa(subSection, this.saxPAASize);

      // Convert the PAA to a string.
      char[] currentString = tsp.ts2String(paa, na.getCuts(this.saxAlphabetSize));

      if (null != previousString) {

        if (NumerosityReductionStrategy.EXACT.equals(this.numerosityReductionStrategy)
            && Arrays.equals(previousString, currentString)) {
          // NumerosityReduction
          continue;
        }
        else if (NumerosityReductionStrategy.MINDIST.equals(this.numerosityReductionStrategy)
            && sp.checkMinDistIsZero(previousString, currentString)) {
          continue;
        }

      }

      previousString = currentString;

      res.put(i, currentString);

      LOGGER.trace(this.id + ", " + String.valueOf(currentString) + ", " + i);

      if (Thread.currentThread().isInterrupted()) {
        LOGGER.info("SAXWorker was interrupted... returning NULL");
        return null;
      }

    }

    // System.out.println(this.id + "@" + res.getSAXString(" "));
    return res;

  }
}
