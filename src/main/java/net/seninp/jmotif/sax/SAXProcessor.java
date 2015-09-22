package net.seninp.jmotif.sax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
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
  public SAXRecords ts2saxViaWindow(double[] ts, int windowSize, int paaSize, double[] cuts,
      NumerosityReductionStrategy strategy, double nThreshold) {

    // the resulting data structure init
    //
    SAXRecords saxFrequencyData = new SAXRecords();

    // scan across the time series extract sub sequences, and convert them to strings
    char[] previousString = null;

    for (int i = 0; i < ts.length - windowSize; i++) {

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
   */
  public SAXRecords ts2saxViaWindowSkipping(double[] ts, int windowSize, int paaSize, double[] cuts,
      NumerosityReductionStrategy strategy, double nThreshold, ArrayList<Integer> skips) {

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
   * Converts the input time series into a SAX data structure via chunking and Z normalization.
   * 
   * @param ts the input data.
   * @param paaSize the PAA size.
   * @param cuts the Alphabet cuts.
   * @param nThreshold the normalization threshold value.
   * 
   * @return SAX representation of the time series.
   */
  public SAXRecords ts2saxByChunking(double[] ts, int paaSize, double[] cuts, double nThreshold) {

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
      saxFrequencyData.add(String.valueOf(c).toCharArray(), i);
    }

    return saxFrequencyData;

  }

  /**
   * Check for mindist.
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
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts the timeseries.
   * @param paaSize the PAA size.
   * @param cuts the alphabet cuts.
   * @param nThreshold the normalization thresholds.
   * 
   * @return The SAX representation for timeseries.
   */
  public char[] ts2string(double[] ts, int paaSize, double[] cuts, double nThreshold) {

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
  public double saxMinDist(char[] a, char[] b, double[][] distanceMatrix) throws SAXException {
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
    String[] allShingles = getAllLists(alphabet, shingleSize);

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
      for (int i = 0; i < word.length() - shingleSize; i++) {
        String shingle = word.substring(i, i + shingleSize);
        res.put(shingle, res.get(shingle) + 1);
      }
    }

    return res;
  }

  /**
   * Computes the distance between approximated values and the real TS.
   * 
   * @param ts the timeseries.
   * @param winSize SAX window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param strategy the numerosity reduction strategy.
   * @param normThreshold the normalization threshold.
   * @return the distance value.
   * @throws Exception if error occurs.
   */
  public double approximationDistance(double[] ts, int winSize, int paaSize, int alphabetSize,
      NumerosityReductionStrategy strategy, double normThreshold) throws Exception {

    double resDistance = 0d;
    int windowCounter = 0;

    NormalAlphabet normalA = new NormalAlphabet();
    char[] previousString = null;

    double pointsPerSegment = (double) winSize / (double) paaSize;

    for (int i = 0; i < ts.length - (winSize - 1); i++) {

      double[] subSection = Arrays.copyOfRange(ts, i, i + winSize);

      if (tsProcessor.stDev(subSection) > normThreshold) {
        subSection = tsProcessor.znorm(subSection, normThreshold);
      }
      double[] paa = tsProcessor.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = tsProcessor.ts2String(paa, normalA.getCuts(alphabetSize));

      // Check if need to leave the loop due to numerosity reduction
      if (NumerosityReductionStrategy.EXACT.equals(strategy)
          && Arrays.equals(previousString, currentString)) {
        continue;
      }
      else if ((null != previousString) && NumerosityReductionStrategy.MINDIST.equals(strategy)) {
        double dist = saxMinDist(previousString, currentString,
            normalA.getDistanceMatrix(alphabetSize));
        if (0.0D == dist) {
          continue;
        }
      }
      previousString = currentString;
      windowCounter++;

      // if made it here compute the distance
      for (int j = 0; j < subSection.length; j++) {
        int paaIdx = (int) Math.round((double) j / pointsPerSegment);
        if (paaIdx >= paaSize) {
          paaIdx = paaSize - 1;
        }
        resDistance = resDistance + ed.distance(paa[paaIdx], subSection[j]);
      }
    }
    return resDistance / (double) windowCounter;
  }

  /**
   * Convert a time series into a shingled representation.
   * 
   * @param data the input data.
   * @param windowSize SAX window size.
   * @param paaSize SAX paa size.
   * @param alphabetSize SAX alphabet size.
   * @param strategy SAX NR strategy.
   * @param normalizationThreshold SAX normalization threshold.
   * @return shingled representation.
   * @throws SAXException if error occurs.
   */
  public Map<String, List<double[]>> toShingles(Map<String, ArrayList<double[]>> data,
      int windowSize, int paaSize, int alphabetSize, NumerosityReductionStrategy strategy,
      double normalizationThreshold) throws SAXException {

    HashMap<String, List<double[]>> res = new HashMap<String, List<double[]>>();

    // build all shingles
    //
    String[] alphabet = new String[alphabetSize];
    for (int i = 0; i < alphabetSize; i++) {
      alphabet[i] = String.valueOf(TSProcessor.ALPHABET[i]);
    }
    String[] allStrings = getAllLists(alphabet, paaSize);

    // and make an index table
    //
    int len = allStrings.length;
    HashMap<String, Integer> indexTable = new HashMap<String, Integer>();
    for (int i = 0; i < allStrings.length; i++) {
      indexTable.put(allStrings[i], i);
    }

    // // some info printout
    // System.out.println("Using " + allStrings.length + " words: "
    // + Arrays.toString(allStrings).replace(", ", "\", \""));

    // iterate ofer all training series
    //
    for (Entry<String, ArrayList<double[]>> e : data.entrySet()) {
      // System.out.println(e.getKey());
      for (double[] series : e.getValue()) {

        // discretize the timeseries
        SAXRecords saxData = ts2saxViaWindow(series, windowSize, paaSize, na.getCuts(alphabetSize),
            strategy, normalizationThreshold);

        // allocate the weights array corresponding to the timeseries
        double[] weights = new double[len];

        // fill in the counts
        for (SAXRecord sr : saxData) {
          String word = String.valueOf(sr.getPayload());
          Integer idx = indexTable.get(word);
          if (null == idx) {
            // System.out.println(word);
          }
          weights[idx] = sr.getIndexes().size();
        }

        // normalize and save that series shingle
        if (!res.containsKey(e.getKey())) {
          res.put(e.getKey(), new ArrayList<double[]>());
        }
        res.get(e.getKey()).add(tsProcessor.normOne(weights));

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
  public static String[] getAllLists(String[] alphabet, int wordLength) {

    // initialize our returned list with the number of elements calculated above
    String[] allLists = new String[(int) Math.pow(alphabet.length, wordLength)];

    // lists of length 1 are just the original elements
    if (wordLength == 1)
      return alphabet;
    else {
      // the recursion--get all lists of length 3, length 2, all the way up to 1
      String[] allSublists = getAllLists(alphabet, wordLength - 1);

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
    String formatted = formatter.print(duration.toPeriod());

    return formatted;

  }
}
