package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;

/**
 * Implements the motif discovery routines.
 * 
 * @author psenin
 *
 */
public class BruteForceMotifImplementation {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(BruteForceDiscordImplementation.class);

  private static TSProcessor tp = new TSProcessor();

  /**
   * Finds 1-Motif
   * 
   * @param series the input time series.
   * @param motifSize the motif size.
   * @param range the similarity range cut off.
   * @param znormThreshold z-normalization threshold.
   * @return motif's positions.
   * @throws Exception if error occurs.
   */
  public static MotifRecord series2BruteForceMotifs(double[] series, int motifSize, double range,
      double znormThreshold) throws Exception {

    int bestMotifCount = -1;
    int bestMotifLiocation = -1;
    ArrayList<Integer> bestMotifOccurrences = null;

    for (int i = 0; i < (series.length - motifSize); i++) {

      int count = 0;
      ArrayList<Integer> occurrences = new ArrayList<Integer>();

      for (int j = 0; j < (series.length - motifSize); j++) {
        if (isNonTrivialMatch(series, i, j, motifSize, range, znormThreshold)) {
          count++;
          occurrences.add(j);
        }
      }

      if (count > 0 && count > bestMotifCount) {
        bestMotifCount = count;
        bestMotifLiocation = i;
        bestMotifOccurrences = occurrences;
        LOGGER.debug("current best motif at {} with freq {}", bestMotifLiocation,
            occurrences.size());
      }
    }

    return new MotifRecord(bestMotifLiocation, bestMotifOccurrences);

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