package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;

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
   * @param zThreshold z normalization threshold.
   * @return motif's positions.
   * @throws Exception if error occurs.
   */
  public static MotifRecord series2EMMAMotifs(double[] series, int motifSize, double range,
      int paaSize, int alphabetSize, double zThreshold) throws Exception {

    int bestMotifCount = -1;
    int bestMotifLiocation = -1;
    boolean finished = false;

    HashMap<String, ArrayList<Integer>> buckets = new HashMap<String, ArrayList<Integer>>(
        (int) Math.pow(paaSize, alphabetSize));

    for (int i = 0; i < (series.length - motifSize); i++) {
      String sax = String.valueOf(tp.ts2String(
          tp.paa(tp.znorm(tp.subseriesByCopy(series, i, i + motifSize), zThreshold), paaSize),
          normalA.getCuts(alphabetSize)));
      if (null == buckets.get(sax)) {
        buckets.put(sax, new ArrayList<Integer>());
      }
      buckets.get(sax).add(i);
    }

    return null;

  }

  private static boolean isNonTrivialMatch(double[] series, int i, int j, Integer motifSize,
      double range) {

    if (Math.abs(i - j) < motifSize) {
      return false;
    }

    Double dd = eaDistance(series, i, j, motifSize, range);

    if (Double.isFinite(dd)) {
      return true;
    }

    return false;
  }

  private static Double eaDistance(double[] series, int a, int b, Integer motifSize, double range) {

    double cutOff2 = range * range;

    Double res = 0D;
    for (int i = 0; i < motifSize; i++) {
      res = res + distance2(series[a + i], series[b + i]);
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