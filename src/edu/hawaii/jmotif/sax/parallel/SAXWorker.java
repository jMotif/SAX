package edu.hawaii.jmotif.sax.parallel;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXProcessor;
import edu.hawaii.jmotif.sax.TSProcessor;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;

/**
 * A callable worker class that will translate a given interval of the timeseries into SAX.
 * 
 * @author psenin
 * 
 */
public class SAXWorker implements Callable<SAXRecords> {

  /** The worker ID. */
  private long id;

  /** The input timeseries. */
  private double[] data;

  /** The conversion start index. */
  private int intervalStart;

  /** The conversion end index. */
  private int intervalEnd;

  /** The SAX discretization sliding window parameter size. */
  private int saxWindowSize;

  /** The SAX discretization PAA size. */
  private int saxPAASize;

  /** The SAX discretization alphabet size. */
  private int saxAlphabetSize;

  /** The SAX discretization sliding window parameter size. */
  private NumerosityReductionStrategy numerosityReductionStrategy;

  /** The SAX discretization normalization threshold. */
  private double normalizationThreshold;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXWorker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Performs SAX discretization.
   * 
   * @param id the job id.
   * @param data the data array.
   * @param intervalStart from which coordinate to start the conversion.
   * @param intervalEnd where to end conversion (non-ionclusive).
   * @param offset the offset for final values of SAX word positions.
   * @param windowSize SAX window size.
   * @param paaSize SAX paa size.
   * @param alphabetSize SAX alphabet size.
   * @param nrs The numerosity reduction strategy.
   * @param normalizationThreshold The normalization strategy.
   */
  public SAXWorker(long id, double[] data, int intervalStart, int intervalEnd, int windowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy nrs, double normalizationThreshold) {
    super();
    this.id = id;
    this.data = data;
    this.intervalStart = intervalStart;
    this.intervalEnd = intervalEnd;
    this.saxWindowSize = windowSize;
    this.saxPAASize = paaSize;
    this.saxAlphabetSize = alphabetSize;
    this.numerosityReductionStrategy = nrs;
    this.normalizationThreshold = normalizationThreshold;
    consoleLogger.debug("sax worker instance id " + this.id + ", data " + this.data.length
        + ", window " + this.saxWindowSize + ", paa " + this.saxPAASize + ", alphabet "
        + this.saxAlphabetSize + ", nr " + this.numerosityReductionStrategy.toString()
        + ", threshold: " + normalizationThreshold + ", start: " + this.intervalStart + ", end: "
        + this.intervalEnd);
  }

  @Override
  public SAXRecords call() throws Exception {

    NormalAlphabet na = new NormalAlphabet();
    TSProcessor tsp = new TSProcessor();
    SAXProcessor sp = new SAXProcessor();

    SAXRecords res = new SAXRecords(this.id);

    if (this.data.length < this.saxWindowSize) {
      return res;
    }

    // scan across the time series extract sub sequences, and convert
    // them to strings
    char[] previousString = null;
    for (int i = this.intervalStart; i < this.intervalEnd - (this.saxWindowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(this.data, i, i + this.saxWindowSize);

      // Z normalize it
      subSection = tsp.znorm(subSection, normalizationThreshold);

      // perform PAA conversion if needed
      double[] paa = tsp.paa(subSection, this.saxPAASize);

      // Convert the PAA to a string.
      char[] currentString = ts2String(paa, normalA.getCuts(this.saxAlphabetSize));

      if (NumerosityReductionStrategy.EXACT.equals(this.numerosityReductionStrategy)
          && Arrays.equals(previousString, currentString)) {
        continue;
      }
      else if ((null != previousString)
          && NumerosityReductionStrategy.MINDIST.equals(this.numerosityReductionStrategy)) {
        double dist = saxMinDist(previousString, currentString,
            normalA.getDistanceMatrix(this.saxAlphabetSize));
        if (0.0D == dist) {
          continue;
        }
      }

      previousString = currentString;

      res.add(currentString, i);
      consoleLogger.trace(this.id + ", " + String.valueOf(currentString) + ", " + i);

    }

    return res;

  }
}
