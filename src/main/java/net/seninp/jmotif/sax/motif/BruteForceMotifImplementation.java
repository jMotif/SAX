package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.distance.EuclideanDistance;
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
  private static EuclideanDistance ed = new EuclideanDistance();

  public static int eaCounter;
  public static int distCounter;

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

      if (count > 0) {
        if (count > bestMotifCount) {
          bestMotifCount = count;
          bestMotifLiocation = i;
          bestMotifOccurrences = occurrences;
          LOGGER.debug("current best motif at {} with freq {}", bestMotifLiocation,
              occurrences.size());
        }
        else if (count == bestMotifCount) {

          LOGGER.debug(" ** its's a tie, checking for variation...");

          double[] motifA = tp.subseriesByCopy(series, bestMotifLiocation,
              bestMotifLiocation + motifSize);
          double[] distancesA = new double[count];

          double[] motifB = tp.subseriesByCopy(series, i, i + motifSize);
          double[] distancesB = new double[count];

          for (int j = 0; j < count; j++) {

            Integer locA = bestMotifOccurrences.get(j);
            double distA = ed.distance(tp.znorm(motifA, znormThreshold),
                tp.znorm(tp.subseriesByCopy(series, locA, locA + motifSize), znormThreshold));
            distancesA[j] = distA;

            Integer locB = occurrences.get(j);
            double distB = ed.distance(tp.znorm(motifB, znormThreshold),
                tp.znorm(tp.subseriesByCopy(series, locB, locB + motifSize), znormThreshold));
            distancesB[j] = distB;

          }

          double varA = tp.var(distancesA);
          double varB = tp.var(distancesB);

          if (varB < varA) {
            bestMotifCount = count;
            bestMotifLiocation = i;
            bestMotifOccurrences = occurrences;
            LOGGER.debug("updated current best motif to one at {} with freq {}", bestMotifLiocation,
                occurrences.size());
          }
        }
      }
    }
    return new MotifRecord(bestMotifLiocation, bestMotifOccurrences);

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