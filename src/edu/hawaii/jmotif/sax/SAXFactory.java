package edu.hawaii.jmotif.sax;

import java.util.Arrays;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.logic.StackTrace;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * Implements SAX algorithms.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXFactory {

  public static final int DEFAULT_COLLECTION_SIZE = 50;

  private static Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.DEBUG;

  private static final double NORMALIZATION_THRESHOLD = 0.005D;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   */
  private SAXFactory() {
    super();
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. NOSKIP means that ALL SAX words reported.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZnormByCutsNoSkip(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. Not all SAX words reported, if the new SAX word is the same as current it will not
   * be reported.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZnormByCuts(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      // if (4728 < i && i < 4732) {
      // System.out.println(i);
      // System.out.println("series "
      // + Arrays.toString(ts.subsection(i, i + windowSize - 1).values()));
      // System.out.println("norm " + Arrays.toString(subSection.values()));
      // System.out.println("paa " + Arrays.toString(paa.values()));
      // System.out.println("str " + String.valueOf(currentString));
      // }
      if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. NOSKIP means that ALL SAX words reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSkip(double[] s, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {
    long[] ticks = new long[s.length];
    for (int i = 0; i < s.length; i++) {
      ticks[i] = i;
    }
    Timeseries ts = new Timeseries(s, ticks);
    return ts2saxZnormByCutsNoSkip(ts, windowSize, paaSize, cuts);
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. Not all SAX words reported, if the new SAX word is the same as current it will not
   * be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCuts(double[] s, int windowSize, int paaSize, double[] cuts)
      throws TSException, CloneNotSupportedException {
    long[] ticks = new long[s.length];
    for (int i = 0; i < s.length; i++) {
      ticks[i] = i;
    }
    Timeseries ts = new Timeseries(s, ticks);
    return ts2saxZnormByCuts(ts, windowSize, paaSize, cuts);
  }

  /**
   * Convert the timeseries into SAX string representation - NO SLIDING WINDOW, i.e CHUNKING -
   * normalizes each of the pieces before SAX conversion. Not all SAX words reported, if the new SAX
   * word is the same as current it will not be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSliding(double[] s, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    int i = 0;
    while (i <= s.length - windowSize - 1) {
      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(s, i, i + windowSize);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      double[] paa = TSUtils.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString))) {
        i = i + windowSize;
        continue;
      }

      previousString = new String(currentString);
      res.add(currentString, i);

      // dont forget the counter
      //
      i = i + windowSize;
    }
    if (i < s.length) {

    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation - NO SLIDING WINDOW, i.e CHUNKING -
   * normalizes each of the pieces before SAX conversion. Not all SAX words reported, if the new SAX
   * word is the same as current it will not be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSlidingNoSkip(double[] s, int windowSize,
      int paaSize, double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();

    // scan across the time series extract sub sequences, and converting
    // them to strings
    int i = 0;
    while (i <= s.length - windowSize - 1) {
      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(s, i, i + windowSize);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      double[] paa = TSUtils.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, cuts);

      res.add(currentString, i);

      // dont forget the counter
      //
      i = i + windowSize;
    }
    if (i < s.length) {

    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation. It doesn't normalize anything.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   */

  public static SAXRecords ts2saxNoZnormByCuts(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      // subSection = TSUtils.normalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      if (!(previousString.isEmpty()) && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZNorm(Timeseries ts, int windowSize, int paaSize,
      Alphabet alphabet, int alphabetSize) throws TSException, CloneNotSupportedException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    return ts2saxZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   */

  public static SAXRecords ts2saxNoZnorm(Timeseries ts, int windowSize, int paaSize,
      Alphabet alphabet, int alphabetSize) throws TSException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    return ts2saxNoZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

  }

  public static SAXRecords data2sax(double[] ts, int slidingWindowSize, int paaSize,
      int alphabetSize) throws TSException {
    NormalAlphabet normalA = new NormalAlphabet();
    String previousString = "";
    SAXRecords res = new SAXRecords();
    for (int i = 0; i < ts.length - (slidingWindowSize - 1); i++) {
      double[] subSection = Arrays.copyOfRange(ts, i, i + slidingWindowSize);
      if (TSUtils.stDev(subSection) > NORMALIZATION_THRESHOLD) {
        subSection = TSUtils.zNormalize(subSection);
      }
      double[] paa = TSUtils.optimizedPaa(subSection, paaSize);
      char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(alphabetSize));
      if (!(previousString.isEmpty()) && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static String ts2string(Timeseries ts, int paaSize, Alphabet alphabet, int alphabetSize)
      throws TSException, CloneNotSupportedException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    int tsLength = ts.size();
    if (tsLength == paaSize) {
      return new String(TSUtils.ts2String(TSUtils.zNormalize(ts), alphabet, alphabetSize));
    }
    else {
      // perform PAA conversion
      Timeseries PAA;
      try {
        PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }
      return new String(TSUtils.ts2String(PAA, alphabet, alphabetSize));
    }
  }

  /**
   * Compute the distance between the two strings, this function use the numbers associated with
   * ASCII codes, i.e. distance between a and b would be 1.
   * 
   * @param a The first string.
   * @param b The second string.
   * @return The pairwise distance.
   * @throws TSException if length are differ.
   */
  public static int strDistance(char[] a, char[] b) throws TSException {
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
      throw new TSException("Unable to compute SAX distance, string lengths are not equal");
    }
  }

  /**
   * Compute the distance between the two chars based on the ASCII symbol codes.
   * 
   * @param a The first char.
   * @param b The second char.
   * @return The distance.
   */
  public static int strDistance(char a, char b) {
    return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
  }

  /**
   * This function implements SAX MINDIST function which uses alphabet based distance matrix.
   * 
   * @param a The SAX string.
   * @param b The SAX string.
   * @param distanceMatrix The distance matrix to use.
   * @return distance between strings.
   * @throws TSException If error occurs.
   */
  public static double saxMinDist(char[] a, char[] b, double[][] distanceMatrix) throws TSException {
    if (a.length == b.length) {
      double dist = 0.0D;
      for (int i = 0; i < a.length; i++) {
        if (Character.isLetter(a[i]) && Character.isLetter(b[i])) {
          int numA = Character.getNumericValue(a[i]) - 10;
          int numB = Character.getNumericValue(b[i]) - 10;
          if (numA > 19 || numA < 0 || numB > 19 || numB < 0) {
            throw new TSException("The character index greater than 19 or less than 0!");
          }
          double localDist = distanceMatrix[numA][numB];
          dist += localDist;
        }
        else {
          throw new TSException("Non-literal character found!");
        }
      }
      return dist;
    }
    else {
      throw new TSException("Data arrays lengths are not equal!");
    }
  }

  /**
   * Convert real-valued series into symbolic representation.
   * 
   * @param vals Real valued timeseries.
   * @param windowSize The PAA window size.
   * @param cuts The cut values array used for SAX transform.
   * @return The symbolic representation of the given real time-series.
   * @throws TSException If error occurs.
   */
  public static char[] getSaxVals(double[] vals, int windowSize, double[] cuts) throws TSException {
    char[] saxVals;
    double std = TSUtils.stDev(vals);
    if (std > NORMALIZATION_THRESHOLD) {
      if (windowSize == cuts.length + 1) {
        saxVals = TSUtils.ts2String(TSUtils.zNormalize(vals), cuts);
      }
      else {
        saxVals = TSUtils.ts2String(TSUtils.zNormalize(TSUtils.paa(vals, cuts.length + 1)), cuts);
      }
    }
    else {
      if (windowSize == cuts.length + 1) {
        saxVals = TSUtils.ts2String(vals, cuts);
      }
      else {
        saxVals = TSUtils.ts2String(TSUtils.paa(vals, cuts.length + 1), cuts);
      }
    }
    return saxVals;
  }

  /**
   * Extracts sub-series from series.
   * 
   * @param data The series.
   * @param start The start position.
   * @param end The end position
   * @return sub-series from start to end.
   */
  // public static double[] getSubSeries(double[] data, int start, int end) {
  // return Arrays.copyOfRange(data, start, end);
  // // double[] vals = new double[end - start];
  // // for (int i = start; i < end; i++) {
  // // vals[i] = data[i];
  // // }
  // // return vals;
  // }

  /**
   * Generic method to convert the milliseconds into the elapsed time string.
   * 
   * @param start Start timestamp.
   * @param finish End timestamp.
   * @return String representation of the elapsed time.
   */
  public static String timeToString(long start, long finish) {
    long diff = finish - start;

    long secondInMillis = 1000;
    long minuteInMillis = secondInMillis * 60;
    long hourInMillis = minuteInMillis * 60;
    long dayInMillis = hourInMillis * 24;
    long yearInMillis = dayInMillis * 365;

    @SuppressWarnings("unused")
    long elapsedYears = diff / yearInMillis;
    diff = diff % yearInMillis;

    @SuppressWarnings("unused")
    long elapsedDays = diff / dayInMillis;
    diff = diff % dayInMillis;

    // @SuppressWarnings("unused")
    long elapsedHours = diff / hourInMillis;
    diff = diff % hourInMillis;

    long elapsedMinutes = diff / minuteInMillis;
    diff = diff % minuteInMillis;

    long elapsedSeconds = diff / secondInMillis;
    diff = diff % secondInMillis;

    long elapsedMilliseconds = diff % secondInMillis;

    return elapsedHours + "h " + elapsedMinutes + "m " + elapsedSeconds + "s "
        + elapsedMilliseconds + "ms";
  }
}
