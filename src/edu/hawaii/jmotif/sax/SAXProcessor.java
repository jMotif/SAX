package edu.hawaii.jmotif.sax;

import java.util.Arrays;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Implements SAX algorithms.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXProcessor {

  private static Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.DEBUG;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXProcessor.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  private TSProcessor tsProcessor;

  /**
   * Constructor.
   */
  private SAXProcessor() {
    super();
    this.tsProcessor = new TSProcessor();
  }

  /**
   * Converts the input time series into a SAX data structure via sliding window and Z
   * normalization.
   * 
   * @param ts the input data.
   * @param windowSize the sliding window size.
   * @param paaSize the PAA size.
   * @param cuts the Alphabet cuts.
   * @param nThreshold the normalization threshold value.
   * @param strategy the NR strategy.
   * 
   * @return SAX representation of the time series.
   */
  public SAXRecords ts2sax(double[] ts, int windowSize, int paaSize, double[] cuts,
      double nThreshold, NumerosityReductionStrategy strategy) {

    // the resulting data structure init
    //
    SAXRecords saxFrequencyData = new SAXRecords();

    // scan across the time series extract sub sequences, and convert them to strings

    char[] previousString = null;

    for (int i = 0; i < ts.length - (windowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(ts, i, i + windowSize);

      // Z normalize it
      subSection = tsProcessor.znorm(subSection, nThreshold);

      // perform PAA conversion if needed
      double[] paa = tsProcessor.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = tsProcessor.ts2String(paa, cuts);

      if (null != previousString) {

        if (NumerosityReductionStrategy.EXACT.equals(strategy)
            && Arrays.equals(previousString, currentString)) {
          // NumerosityReduction
          continue;
        }
        else if (NumerosityReductionStrategy.MINDIST.equals(strategy)
            && minDistIsZero(previousString, currentString)) {
          continue;
        }

      }

      previousString = currentString;

      saxFrequencyData.add(currentString, i);
    }

    return saxFrequencyData;

  }

  /**
   * 
   * @param previousString
   * @param currentString
   * @return
   */
  private boolean minDistIsZero(char[] a, char[] b) {
    for (int i = 0; i < a.length; i++) {
      if (charDistance(a[i], b[i]) > 1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compute the distance between the two chars based on the ASCII symbol codes.
   * 
   * @param a The first char.
   * @param b The second char.
   * @return The distance.
   */
  public static int charDistance(char a, char b) {
    return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
  }

  /**
   * Compute the distance between the two strings, this function use the numbers associated with
   * ASCII codes, i.e. distance between a and b would be 1.
   * 
   * @param a The first string.
   * @param b The second string.
   * @return The pairwise distance.
   * @throws SAXException if length are differ.
   */
  public static int strDistance(char[] a, char[] b) throws SAXException {
    if (a.length == b.length) {
      int distance = 0;
      for (int i = 0; i < a.length; i++) {
        int tDist = Math.abs(Character.getNumericValue(a[i]) - Character.getNumericValue(b[i]));
        if (tDist > 1) {
          distance += tDist;
        }
      }
      return distance;
    }
    else {
      throw new SAXException("Unable to compute SAX distance, string lengths are not equal");
    }
  }

  /**
   * This function implements SAX MINDIST function which uses alphabet based distance matrix.
   * 
   * @param a The SAX string.
   * @param b The SAX string.
   * @param distanceMatrix The distance matrix to use.
   * @return distance between strings.
   * @throws SAXException If error occurs.
   */
  public static double saxMinDist(char[] a, char[] b, double[][] distanceMatrix)
      throws SAXException {
    if (a.length == b.length) {
      double dist = 0.0D;
      for (int i = 0; i < a.length; i++) {
        if (Character.isLetter(a[i]) && Character.isLetter(b[i])) {
          int numA = Character.getNumericValue(a[i]) - 10;
          int numB = Character.getNumericValue(b[i]) - 10;
          if (numA > 19 || numA < 0 || numB > 19 || numB < 0) {
            throw new SAXException("The character index greater than 19 or less than 0!");
          }
          double localDist = distanceMatrix[numA][numB];
          dist += localDist;
        }
        else {
          throw new SAXException("Non-literal character found!");
        }
      }
      return dist;
    }
    else {
      throw new SAXException("Data arrays lengths are not equal!");
    }
  }

}
