package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.util.JmotifMapEntry;

/**
 * Implements the motif discovery routines.
 * 
 * @author psenin
 *
 */
public class EMMAImplementation {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(EMMAImplementation.class);

  private static TSProcessor tp = new TSProcessor();
  private static SAXProcessor sp = new SAXProcessor();
  private static NormalAlphabet normalA = new NormalAlphabet();

  private static EuclideanDistance ed = new EuclideanDistance();

  public static int eaCounter;
  public static int distCounter;

  /**
   * Finds 1-Motif
   * 
   * @param series the input time series.
   * @param motifSize the motif size.
   * @param range the similarity range cut off.
   * @param paaSize the PAA size.
   * @param alphabetSize the alphabet size.
   * @param znormThreshold z normalization threshold.
   * @return motif's positions.
   * @throws Exception if error occurs.
   */
  public static MotifRecord series2EMMAMotifs(double[] series, int motifSize, double range,
      int paaSize, int alphabetSize, double znormThreshold) throws Exception {

    MotifRecord res = new MotifRecord(-1, new ArrayList<Integer>());
    boolean finished = false;

    HashMap<String, ArrayList<Integer>> buckets = new HashMap<String, ArrayList<Integer>>(
        (int) Math.pow(paaSize, alphabetSize));

    for (int i = 0; i < (series.length - motifSize); i++) {
      String sax = String.valueOf(tp.ts2String(
          tp.paa(tp.znorm(tp.subseriesByCopy(series, i, i + motifSize), znormThreshold), paaSize),
          normalA.getCuts(alphabetSize)));
      if (null == buckets.get(sax)) {
        buckets.put(sax, new ArrayList<Integer>());
      }
      buckets.get(sax).add(i);
    }

    ArrayList<JmotifMapEntry<Integer, String>> bucketsOrder = new ArrayList<JmotifMapEntry<Integer, String>>(
        buckets.size());
    for (Entry<String, ArrayList<Integer>> e : buckets.entrySet()) {
      bucketsOrder.add(new JmotifMapEntry<Integer, String>(e.getValue().size(), e.getKey()));
    }

    Collections.sort(bucketsOrder, new Comparator<JmotifMapEntry<Integer, String>>() {
      public int compare(JmotifMapEntry<Integer, String> a, JmotifMapEntry<Integer, String> b) {
        return b.getKey().compareTo(a.getKey());
      }
    });

    double[][] dm = normalA.getDistanceMatrix(alphabetSize);
    int currBucketIdx = 0;

    JmotifMapEntry<Integer, String> MPC = bucketsOrder.get(currBucketIdx);
    ArrayList<Integer> neighborhood = new ArrayList<Integer>(buckets.get(MPC.getValue()));

    while (!(finished) && (currBucketIdx < bucketsOrder.size()) && (neighborhood.size() > 2)) {

      if (currBucketIdx < (bucketsOrder.size() - 1)) {
        for (int i = currBucketIdx + 1; i < bucketsOrder.size(); i++) {
          String cWord = bucketsOrder.get(i).getValue();
          if (range > sp.saxMinDist(MPC.getValue().toCharArray(), cWord.toCharArray(), dm,
              motifSize, paaSize)) {
            neighborhood.addAll(buckets.get(cWord));
          }
        }
      }

      LOGGER.debug("current bucket {} at {}", MPC.getValue(), neighborhood);

      MotifRecord tmpRes = ADM(series, neighborhood, motifSize, range, znormThreshold);

      LOGGER.debug("current tmp motif {} ", tmpRes.toString());

      if (tmpRes.getFrequency() > res.getFrequency() || res.isEmpty()) {
        res = tmpRes;
        LOGGER.debug("updating the best motif to {} ", res.toString());
      }
      else if (tmpRes.getFrequency() == res.getFrequency() && !(res.isEmpty())) {

        LOGGER.debug(" ** its's a tie, checking for variation...");

        double[] motifA = tp.subseriesByCopy(series, res.getLocation(),
            res.getLocation() + motifSize);
        double[] distancesA = new double[res.getFrequency()];

        double[] motifB = tp.subseriesByCopy(series, tmpRes.getLocation(),
            tmpRes.getLocation() + motifSize);
        double[] distancesB = new double[res.getFrequency()];

        ArrayList<Integer> bestMotifOccurrences = res.getOccurrences();
        ArrayList<Integer> tmpMotifOccurrences = tmpRes.getOccurrences();
        for (int j = 0; j < res.getFrequency(); j++) {

          Integer locA = bestMotifOccurrences.get(j);
          double distA = ed.distance(tp.znorm(motifA, znormThreshold),
              tp.znorm(tp.subseriesByCopy(series, locA, locA + motifSize), znormThreshold));
          distancesA[j] = distA;

          Integer locB = tmpMotifOccurrences.get(j);
          double distB = ed.distance(tp.znorm(motifB, znormThreshold),
              tp.znorm(tp.subseriesByCopy(series, locB, locB + motifSize), znormThreshold));
          distancesB[j] = distB;

        }

        double varA = tp.var(distancesA);
        double varB = tp.var(distancesB);

        if (varB < varA) {
          LOGGER.debug("updated current best motif to {}", tmpRes);
          res = tmpRes;
        }

      }

      if ((currBucketIdx < (bucketsOrder.size() - 1))
          && (tmpRes.getFrequency() > bucketsOrder.get(currBucketIdx + 1).getKey())) {
        finished = true;
      }
      else {
        currBucketIdx++;
        if (currBucketIdx == bucketsOrder.size()) {
          // we processed all buckets up in here -- break out
          break;
        }
        MPC = bucketsOrder.get(currBucketIdx);
        neighborhood = new ArrayList<Integer>(buckets.get(MPC.getValue()));
      }

    }

    return res;

  }

  /**
   * This is not a real ADM implementation.
   * 
   * @param series the input timeseries.
   * @param neighborhood the neighborhood coordinates.
   * @param motifSize the motif size.
   * @param range the range value.
   * @param znormThreshold z-normalization threshold.
   * @return the best motif record found within the neighborhood.
   * @throws Exception if error occurs.
   * 
   */
  private static MotifRecord ADM(double[] series, ArrayList<Integer> neighborhood, int motifSize,
      double range, double znormThreshold) throws Exception {

    MotifRecord res = new MotifRecord(-1, new ArrayList<Integer>());

    ArrayList<BitSet> admDistances = new ArrayList<BitSet>(neighborhood.size());
    for (int i = 0; i < neighborhood.size(); i++) {
      admDistances.add(new BitSet(i));
    }

    for (int i = 0; i < neighborhood.size(); i++) {
      for (int j = 0; j < i; j++) { // diagonal wouldn't count anyway
        boolean isMatch = isNonTrivialMatch(series, neighborhood.get(i), neighborhood.get(j),
            motifSize, range, znormThreshold);
        if (isMatch) {
          admDistances.get(i).set(j);
          admDistances.get(j).set(i);
        }
      }
    }

    int maxCount = 0;
    for (int i = 0; i < neighborhood.size(); i++) {

      int tmpCounter = 0;

      for (int j = 0; j < neighborhood.size(); j++) {
        if (admDistances.get(i).get(j)) {
          tmpCounter++;
        }
      }

      if (tmpCounter > maxCount) {
        maxCount = tmpCounter;
        ArrayList<Integer> occurrences = new ArrayList<>();
        for (int j = 0; j < neighborhood.size(); j++) {
          if (admDistances.get(i).get(j)) {
            occurrences.add(neighborhood.get(j));
          }
        }
        res = new MotifRecord(neighborhood.get(i), occurrences);
      }

    }

    return res;

  }

  /**
   * Checks for the overlap and the range-configured distance.
   * 
   * @param series the series to use.
   * @param i the position of subseries a.
   * @param j the position of subseries b.
   * @param motifSize the motif length.
   * @param range the range value.
   * @param znormThreshold z-normalization threshold.
   * @return true if all is cool, false if overlaps or above the range value.
   */
  private static boolean isNonTrivialMatch(double[] series, int i, int j, Integer motifSize,
      double range, double znormThreshold) {

    if (Math.abs(i - j) < motifSize) {
      return false;
    }

    Double dd = eaDistance(series, i, j, motifSize, range, znormThreshold);

    if (Double.isFinite(dd)) {
      return true;
    }

    return false;
  }

  /**
   * Early abandoning distance configure by range.
   * 
   * @param series the series to use.
   * @param a the position of subseries a.
   * @param b the position of subseries b.
   * @param motifSize the motif length.
   * @param range the range value.
   * @param znormThreshold z-normalization threshold.
   * @return a distance value or NAN if above the threshold.
   */
  private static Double eaDistance(double[] series, int a, int b, Integer motifSize, double range,
      double znormThreshold) {

    distCounter++;

    double cutOff2 = range * range;

    double[] seriesA = tp.znorm(tp.subseriesByCopy(series, a, a + motifSize), znormThreshold);
    double[] seriesB = tp.znorm(tp.subseriesByCopy(series, b, b + motifSize), znormThreshold);

    Double res = 0D;
    for (int i = 0; i < motifSize; i++) {
      res = res + distance2(seriesA[i], seriesB[i]);
      if (res > cutOff2) {
        eaCounter++;
        return Double.NaN;
      }
    }
    return Math.sqrt(res);

  }

  /**
   * Distance square.
   * 
   * @param p1 point1.
   * @param p2 point2.
   * @return the distance square.
   */
  private static double distance2(double p1, double p2) {
    return (p1 - p2) * (p1 - p2);
  }
}