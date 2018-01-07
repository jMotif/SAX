package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.util.JmotifMapEntry;

/**
 * Implements the motif discovery routines.
 * 
 * @author psenin
 *
 */
public class EMMAImplementation {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(BruteForceDiscordImplementation.class);

  private static TSProcessor tp = new TSProcessor();
  private static SAXProcessor sp = new SAXProcessor();
  private static NormalAlphabet normalA = new NormalAlphabet();

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

    while (!(finished) && (currBucketIdx < (bucketsOrder.size())) && (neighborhood.size() > 2)) {

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
      if (neighborhood.contains(140)) {
        LOGGER.debug(" ***");
      }

      MotifRecord tmpRes = ADM(series, neighborhood, motifSize, range, znormThreshold);

      if (tmpRes.getFrequency() > res.getFrequency()) {
        res = tmpRes;
      }

      if ((currBucketIdx < (bucketsOrder.size() - 1))
          && (tmpRes.getFrequency() > bucketsOrder.get(currBucketIdx + 1).getKey())) {
        finished = true;
      }
      else {
        currBucketIdx++;
        MPC = bucketsOrder.get(currBucketIdx);
        neighborhood = new ArrayList<Integer>(buckets.get(MPC.getValue()));
      }

    }

    return res;

  }

  private static MotifRecord ADM(double[] series, ArrayList<Integer> neighborhood, int motifSize,
      double range, double znormThreshold) {

    MotifRecord res = new MotifRecord(-1, new ArrayList<Integer>());
    boolean[][] admDistances = new boolean[neighborhood.size()][neighborhood.size()];

    for (int i = 0; i < neighborhood.size(); i++) {
      for (int j = 0; j <= i; j++) {
        admDistances[i][j] = isNonTrivialMatch(series, neighborhood.get(i), neighborhood.get(j),
            motifSize, range, znormThreshold);
        admDistances[j][i] = admDistances[i][j];
      }
    }

    int maxCount = 0;
    for (int i = 0; i < neighborhood.size(); i++) {
      if (140 == neighborhood.get(i)) {
        LOGGER.debug(Arrays.toString(admDistances[i]));
      }
      int tmpCounter = 0;
      for (int j = 0; j < neighborhood.size(); j++) {
        if (admDistances[i][j]) {
          tmpCounter++;
        }
      }
      if (tmpCounter > maxCount) {
        maxCount = tmpCounter;
        ArrayList<Integer> occurrences = new ArrayList<>();
        for (int j = 0; j < neighborhood.size(); j++) {
          if (admDistances[i][j]) {
            occurrences.add(neighborhood.get(j));
          }
        }
        res = new MotifRecord(neighborhood.get(i), occurrences);
      }
    }

    return res;

  }

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

  private static Double eaDistance(double[] series, int a, int b, Integer motifSize, double range,
      double znormThreshold) {

    double cutOff2 = range * range;

    double[] seriesA = tp.znorm(tp.subseriesByCopy(series, a, a + motifSize), znormThreshold);
    double[] seriesB = tp.znorm(tp.subseriesByCopy(series, b, b + motifSize), znormThreshold);

    Double res = 0D;
    for (int i = 0; i < motifSize; i++) {
      res = res + distance2(seriesA[i], seriesB[i]);
      if (res > cutOff2) {
        return Double.NaN;
      }
    }
    return Math.sqrt(res);

  }

  private static double distance2(double p1, double p2) {
    return (p1 - p2) * (p1 - p2);
  }
}