package net.seninp.jmotif.sax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.bitmap.Shingles;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Implements SAX algorithms.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXProcessor {

  private final TSProcessor tsProcessor;
  private final NormalAlphabet na;
  private EuclideanDistance ed;

  /**
   * Constructor.
   */
  public SAXProcessor() {
    super();
    this.tsProcessor = new TSProcessor();
    this.na = new NormalAlphabet();
    this.ed = new EuclideanDistance();
  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts the timeseries.
   * @param paaSize the PAA size.
   * @param cuts the alphabet cuts.
   * @param nThreshold the normalization thresholds.
   * 
   * @return The SAX representation for timeseries.
   * @throws SAXException if error occurs.
   */
  public char[] ts2string(double[] ts, int paaSize, double[] cuts, double nThreshold)
      throws SAXException {

    if (paaSize == ts.length) {
      return tsProcessor.ts2String(tsProcessor.znorm(ts, nThreshold), cuts);
    }
    else {
      // perform PAA conversion
      double[] paa = tsProcessor.paa(tsProcessor.znorm(ts, nThreshold), paaSize);
      return tsProcessor.ts2String(paa, cuts);
    }
  }

  /**
   * Converts the input time series into a SAX data structure via chunking and Z normalization.
   * 
   * @param ts the input data.
   * @param paaSize the PAA size.
   * @param cuts the Alphabet cuts.
   * @param nThreshold the normalization threshold value.
   * 
   * @return SAX representation of the time series.
   * @throws SAXException if error occurs.
   */
  public SAXRecords ts2saxByChunking(double[] ts, int paaSize, double[] cuts, double nThreshold)
      throws SAXException {

    SAXRecords saxFrequencyData = new SAXRecords();

    // Z normalize it
    double[] normalizedTS = tsProcessor.znorm(ts, nThreshold);

    // perform PAA conversion if needed
    double[] paa = tsProcessor.paa(normalizedTS, paaSize);

    // Convert the PAA to a string.
    char[] currentString = tsProcessor.ts2String(paa, cuts);

    // create the datastructure
    for (int i = 0; i < currentString.length; i++) {
      char c = currentString[i];
      int pos = (int) Math.floor(i * ts.length / currentString.length);
      saxFrequencyData.add(String.valueOf(c).toCharArray(), pos);
    }

    return saxFrequencyData;

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
   * @throws SAXException if error occurs.
   */
  public SAXRecords ts2saxViaWindow(double[] ts, int windowSize, int paaSize, double[] cuts,
      NumerosityReductionStrategy strategy, double nThreshold) throws SAXException {

    if (windowSize > ts.length) {
      throw new SAXException(
          "Unable to saxify via window, window size is greater than the timeseries length...");
    }

    // the resulting data structure init
    //
    SAXRecords saxFrequencyData = new SAXRecords();

    // scan across the time series extract sub sequences, and convert them to strings
    char[] previousString = null;

    for (int i = 0; i <= ts.length - windowSize; i++) {

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
            && checkMinDistIsZero(previousString, currentString)) {
          continue;
        }

      }

      previousString = currentString;

      saxFrequencyData.add(currentString, i);
    }

    // ArrayList<Integer> keys = saxFrequencyData.getAllIndices();
    // for (int i : keys) {
    // System.out.println(i + "," + String.valueOf(saxFrequencyData.getByIndex(i).getPayload()));
    // }

    return saxFrequencyData;

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
   * @param skips The list of points which shall be skipped during conversion; this feature is
   * particularly important when building a concatenated from pieces time series and junction shall
   * not make it into the grammar.
   * 
   * @return SAX representation of the time series.
   * @throws SAXException if error occurs.
   */
  public SAXRecords ts2saxViaWindowSkipping(double[] ts, int windowSize, int paaSize, double[] cuts,
      NumerosityReductionStrategy strategy, double nThreshold, ArrayList<Integer> skips)
      throws SAXException {

    // the resulting data structure init
    //
    SAXRecords saxFrequencyData = new SAXRecords();

    Collections.sort(skips);
    int cSkipIdx = 0;

    // scan across the time series extract sub sequences, and convert them to strings
    char[] previousString = null;
    boolean skipped = false;

    for (int i = 0; i < ts.length - (windowSize - 1); i++) {

      // skip what need to be skipped
      if (cSkipIdx < skips.size() && i == skips.get(cSkipIdx)) {
        cSkipIdx = cSkipIdx + 1;
        skipped = true;
        continue;
      }

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(ts, i, i + windowSize);

      // Z normalize it
      subSection = tsProcessor.znorm(subSection, nThreshold);

      // perform PAA conversion if needed
      double[] paa = tsProcessor.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = tsProcessor.ts2String(paa, cuts);

      if (!(skipped) && null != previousString) {

        if (NumerosityReductionStrategy.EXACT.equals(strategy)
            && Arrays.equals(previousString, currentString)) {
          // NumerosityReduction
          continue;
        }
        else if (NumerosityReductionStrategy.MINDIST.equals(strategy)
            && checkMinDistIsZero(previousString, currentString)) {
          continue;
        }

      }

      previousString = currentString;
      if (skipped) {
        skipped = false;
      }

      saxFrequencyData.add(currentString, i);
    }

    return saxFrequencyData;
  }

  /**
   * Compute the distance between the two chars based on the ASCII symbol codes.
   * 
   * @param a The first char.
   * @param b The second char.
   * @return The distance.
   */
  public int charDistance(char a, char b) {
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
  public int strDistance(char[] a, char[] b) throws SAXException {
    if (a.length == b.length) {
      int distance = 0;
      for (int i = 0; i < a.length; i++) {
        int tDist = Math.abs(Character.getNumericValue(a[i]) - Character.getNumericValue(b[i]));
        distance += tDist;
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
   * @param n the time series length (sliding window length).
   * @param w the number of PAA segments.
   * @return distance between strings.
   * @throws SAXException If error occurs.
   */
  public double saxMinDist(char[] a, char[] b, double[][] distanceMatrix, int n, int w)
      throws SAXException {
    if (a.length == b.length) {
      double dist = 0.0D;
      for (int i = 0; i < a.length; i++) {
        if (Character.isLetter(a[i]) && Character.isLetter(b[i])) {
          // ... forms have numeric values from 10 through 35
          int numA = Character.getNumericValue(a[i]) - 10;
          int numB = Character.getNumericValue(b[i]) - 10;
          int maxIdx = distanceMatrix[0].length;
          if (numA > (maxIdx - 1) || numA < 0 || numB > (maxIdx - 1) || numB < 0) {
            throw new SAXException(
                "The character index greater than " + maxIdx + " or less than 0!");
          }
          double localDist = distanceMatrix[numA][numB];
          dist = dist + localDist * localDist;
        }
        else {
          throw new SAXException("Non-literal character found!");
        }
      }
      return Math.sqrt((double) n / (double) w) * Math.sqrt(dist);
    }
    else {
      throw new SAXException("Data arrays lengths are not equal!");
    }
  }

  /**
   * Check for trivial mindist case.
   * 
   * @param a first string.
   * @param b second string.
   * @return true if mindist between strings is zero.
   */
  public boolean checkMinDistIsZero(char[] a, char[] b) {
    for (int i = 0; i < a.length; i++) {
      if (charDistance(a[i], b[i]) > 1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Computes the distance between approximated values and the real TS.
   * 
   * @param ts the timeseries.
   * @param winSize SAX window size.
   * @param paaSize SAX PAA size.
   * @param normThreshold the normalization threshold.
   * @return the distance value.
   * @throws Exception if error occurs.
   */
  public double approximationDistancePAA(double[] ts, int winSize, int paaSize,
      double normThreshold) throws Exception {

    double resDistance = 0d;
    int windowCounter = 0;

    double pointsPerWindow = (double) winSize / (double) paaSize;

    for (int i = 0; i < ts.length - winSize + 1; i++) {

      double[] subseries = Arrays.copyOfRange(ts, i, i + winSize);

      if (tsProcessor.stDev(subseries) > normThreshold) {
        subseries = tsProcessor.znorm(subseries, normThreshold);
      }

      double[] paa = tsProcessor.paa(subseries, paaSize);

      windowCounter++;

      // essentially the distance here is the distance between the segment's
      // PAA value and the real TS value
      //
      double subsequenceDistance = 0.;
      for (int j = 0; j < subseries.length; j++) {

        int paaIdx = (int) Math.floor(((double) j + 0.5) / (double) pointsPerWindow);
        if (paaIdx < 0) {
          paaIdx = 0;
        }
        if (paaIdx > paa.length) {
          paaIdx = paa.length - 1;
        }

        subsequenceDistance = subsequenceDistance + ed.distance(paa[paaIdx], subseries[j]);
      }

      resDistance = resDistance + subsequenceDistance / subseries.length;
    }
    return resDistance / (double) windowCounter;
  }

  /**
   * Computes the distance between approximated values and the real TS.
   * 
   * @param ts the timeseries.
   * @param winSize SAX window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param normThreshold the normalization threshold.
   * @return the distance value.
   * @throws Exception if error occurs.
   */
  public double approximationDistanceAlphabet(double[] ts, int winSize, int paaSize,
      int alphabetSize, double normThreshold) throws Exception {

    double resDistance = 0d;
    int windowCounter = 0;

    double[] centralLines = na.getCentralCuts(alphabetSize);

    for (int i = 0; i < ts.length - winSize + 1; i++) {

      double[] subseries = Arrays.copyOfRange(ts, i, i + winSize);
      double subsequenceDistance = 0.;

      if (tsProcessor.stDev(subseries) > normThreshold) {
        subseries = tsProcessor.znorm(subseries, normThreshold);
      }

      double[] paa = tsProcessor.paa(subseries, paaSize);
      int[] leterIndexes = tsProcessor.ts2Index(paa, na, alphabetSize);

      windowCounter++;

      // essentially the distance here is the distance between the segment's
      // PAA value and the real TS value
      //
      for (int j = 0; j < paa.length; j++) {
        // compute the alphabet central cut line
        int letterIdx = leterIndexes[j];
        double cLine = centralLines[letterIdx];
        subsequenceDistance = subsequenceDistance + ed.distance(cLine, paa[j]);
      }

      resDistance = resDistance + subsequenceDistance / paa.length;
    }

    return resDistance / (double) windowCounter;
  }

  /**
   * Converts a single time-series into map of shingle frequencies.
   * 
   * @param series the time series.
   * @param windowSize the sliding window size.
   * @param paaSize the PAA segments number.
   * @param alphabetSize the alphabet size.
   * @param strategy the numerosity reduction strategy.
   * @param nrThreshold the SAX normalization threshold.
   * @param shingleSize the shingle size.
   * 
   * @return map of shingle frequencies.
   * @throws SAXException if error occurs.
   */
  public Map<String, Integer> ts2Shingles(double[] series, int windowSize, int paaSize,
      int alphabetSize, NumerosityReductionStrategy strategy, double nrThreshold, int shingleSize)
      throws SAXException {

    // build all shingles
    String[] alphabet = new String[alphabetSize];
    for (int i = 0; i < alphabetSize; i++) {
      alphabet[i] = String.valueOf(TSProcessor.ALPHABET[i]);
    }
    String[] allShingles = getAllPermutations(alphabet, shingleSize);

    // result
    HashMap<String, Integer> res = new HashMap<String, Integer>(allShingles.length);
    for (String s : allShingles) {
      res.put(s, 0);
    }

    // discretize
    SAXRecords saxData = ts2saxViaWindow(series, windowSize, paaSize, na.getCuts(alphabetSize),
        strategy, nrThreshold);

    // fill in the counts
    for (SAXRecord sr : saxData) {
      String word = String.valueOf(sr.getPayload());
      int frequency = sr.getIndexes().size();
      for (int i = 0; i <= word.length() - shingleSize; i++) {
        String shingle = word.substring(i, i + shingleSize);
        res.put(shingle, res.get(shingle) + frequency);
      }
    }

    return res;
  }

  /**
   * Converts a time-series data frame into shingled data frame.
   * 
   * @param data the input data.
   * @param windowSize SAX window size.
   * @param paaSize SAX paa size.
   * @param alphabetSize SAX alphabet size.
   * @param strategy SAX NR strategy.
   * @param normalizationThreshold SAX normalization threshold.
   * @param shingleSize the shingle size.
   * @return shingled representation.
   * @throws SAXException if error occurs.
   */
  public Shingles manySeriesToShingles(Map<String, ArrayList<double[]>> data, int windowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy strategy,
      double normalizationThreshold, int shingleSize) throws SAXException {

    Shingles res = new Shingles(alphabetSize, shingleSize);

    // iterate over all training series
    //
    for (Entry<String, ArrayList<double[]>> e : data.entrySet()) {

      // System.out.println(e.getKey());
      for (double[] series : e.getValue()) {

        // convert the time series into shingles
        Map<String, Integer> shingles = ts2Shingles(series, windowSize, paaSize, alphabetSize,
            strategy, normalizationThreshold, shingleSize);

        // allocate the weights array corresponding to the time series
        int[] counts = new int[res.getIndex().size()];

        // fill in the counts
        for (String str : shingles.keySet()) {
          Integer idx = res.getIndex().get(str);
          counts[idx] = shingles.get(str);
        }

        res.addShingledSeries(e.getKey(), counts);

      }
    }

    return res;

  }

  /**
   * Get all permutations of the given alphabet of given length.
   * 
   * @param alphabet the alphabet to use.
   * @param wordLength the word length.
   * @return set of permutation.
   */
  public static String[] getAllPermutations(String[] alphabet, int wordLength) {

    // initialize our returned list with the number of elements calculated above
    String[] allLists = new String[(int) Math.pow(alphabet.length, wordLength)];

    // lists of length 1 are just the original elements
    if (wordLength == 1)
      return alphabet;
    else {
      // the recursion--get all lists of length 3, length 2, all the way up to 1
      String[] allSublists = getAllPermutations(alphabet, wordLength - 1);

      // append the sublists to each element
      int arrayIndex = 0;

      for (int i = 0; i < alphabet.length; i++) {
        for (int j = 0; j < allSublists.length; j++) {
          // add the newly appended combination to the list
          allLists[arrayIndex] = alphabet[i] + allSublists[j];
          arrayIndex++;
        }
      }

      return allLists;
    }
  }

  /**
   * Generic method to convert the milliseconds into the elapsed time string.
   * 
   * @param start Start timestamp.
   * @param finish End timestamp.
   * @return String representation of the elapsed time.
   */
  public static String timeToString(long start, long finish) {

    Duration duration = new Duration(finish - start); // in milliseconds
    PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix("d")
        .appendHours().appendSuffix("h").appendMinutes().appendSuffix("m").appendSeconds()
        .appendSuffix("s").appendMillis().appendSuffix("ms").toFormatter();

    return formatter.print(duration.toPeriod());

  }
}
