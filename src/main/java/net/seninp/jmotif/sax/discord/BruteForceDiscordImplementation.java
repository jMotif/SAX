package net.seninp.jmotif.sax.discord;

import java.util.Date;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.registry.SlidingWindowMarkerAlgorithm;
import net.seninp.jmotif.sax.registry.VisitRegistry;

/**
 * Implements SAX-based discord finder, i.e. HOT-SAX.
 *
 * @author psenin
 */
public class BruteForceDiscordImplementation {

  // logging stuff
  //
  private static final Logger LOGGER;
  private static final Level LOGGING_LEVEL = Level.INFO;

  static {
    LOGGER = (Logger) LoggerFactory.getLogger(BruteForceDiscordImplementation.class);
    LOGGER.setLevel(LOGGING_LEVEL);
  }

  private static TSProcessor tsProcessor = new TSProcessor();

  private static EuclideanDistance ed = new EuclideanDistance();

  /**
   * Brute force discord search implementation. BRUTE FORCE algorithm.
   *
   * @param series the data we work with.
   * @param windowSize the sliding window size.
   * @param discordCollectionSize the number of discords we look for.
   * @param marker the marker window algorithm implementation.
   * @param nThreshold the z-Normalization threshold value.
   * @return discords.
   * @throws Exception if error occurs.
   */
  public static DiscordRecords series2BruteForceDiscords(double[] series, Integer windowSize,
      int discordCollectionSize, SlidingWindowMarkerAlgorithm marker, double nThreshold)
      throws Exception {

    DiscordRecords discords = new DiscordRecords();

    // init new registry to the full length, but mark the end of it
    //
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);
    globalTrackVisitRegistry.markVisited(series.length - windowSize - 1, series.length);

    int discordCounter = 0;

    while (discords.getSize() < discordCollectionSize) {

      LOGGER.debug("currently known discords: {} out of {}", discords.getSize(),
          discordCollectionSize);

      // mark start and number of iterations
      Date start = new Date();

      DiscordRecord bestDiscord = findBestDiscordBruteForce(series, windowSize,
          globalTrackVisitRegistry, nThreshold);
      bestDiscord.setPayload("#" + discordCounter);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        LOGGER.debug("breaking the outer search loop, discords found: {} last seen discord: {}"
            + discords.getSize(), bestDiscord);
        break;
      }

      bestDiscord.setInfo(
          "position " + bestDiscord.getPosition() + ", NN distance " + bestDiscord.getNNDistance()
              + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime())
              + ", " + bestDiscord.getInfo());
      LOGGER.debug("{}", bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

      discordCounter++;
    }

    // done deal
    //
    return discords;
  }

  /**
   * Finds the best discord. BRUTE FORCE algorithm.
   *
   * @param series the data.
   * @param windowSize the SAX sliding window size.
   * @param globalRegistry the visit registry to use.
   * @param nThreshold the z-Normalization threshold value.
   * @return the best discord with respect to registry.
   * @throws Exception if error occurs.
   */
  private static DiscordRecord findBestDiscordBruteForce(double[] series, Integer windowSize,
      VisitRegistry globalRegistry, double nThreshold) throws Exception {

    Date start = new Date();

    long distanceCallsCounter = 0;

    double bestSoFarDistance = -1.0;
    int bestSoFarPosition = -1;

    VisitRegistry outerRegistry = globalRegistry.clone();

    int outerIdx = -1;
    while (-1 != (outerIdx = outerRegistry.getNextRandomUnvisitedPosition())) { // outer loop

      outerRegistry.markVisited(outerIdx);

      // imo this is a useless piece
      // check the global visits registry
      // if (globalRegistry.isVisited(outerIdx)) {
      // continue;
      // }

      double[] candidateSeq = tsProcessor
          .znorm(tsProcessor.subseriesByCopy(series, outerIdx, outerIdx + windowSize), nThreshold);
      double nearestNeighborDistance = Double.MAX_VALUE;
      VisitRegistry innerRegistry = new VisitRegistry(series.length - windowSize);

      int innerIdx;
      while (-1 != (innerIdx = innerRegistry.getNextRandomUnvisitedPosition())) { // inner loop
        innerRegistry.markVisited(innerIdx);

        if (Math.abs(outerIdx - innerIdx) > windowSize) { // > means they shall not overlap even
                                                          // over a single point

          double[] currentSubsequence = tsProcessor.znorm(
              tsProcessor.subseriesByCopy(series, innerIdx, innerIdx + windowSize), nThreshold);

          double dist = ed.earlyAbandonedDistance(candidateSeq, currentSubsequence,
              nearestNeighborDistance);

          distanceCallsCounter++;

          if ((!Double.isNaN(dist)) && dist < nearestNeighborDistance) {
            nearestNeighborDistance = dist;
          }
        }

      }

      if (!(Double.isInfinite(nearestNeighborDistance))
          && nearestNeighborDistance > bestSoFarDistance) {
        bestSoFarDistance = nearestNeighborDistance;
        bestSoFarPosition = outerIdx;
        LOGGER.trace("discord updated: pos {}, dist {}", bestSoFarPosition, bestSoFarDistance);
      }

    }
    Date firstDiscord = new Date();

    LOGGER.debug("best discord found at {}, best distance: {}, in {} distance calls: {}",
        bestSoFarPosition, bestSoFarDistance,
        SAXProcessor.timeToString(start.getTime(), firstDiscord.getTime()), distanceCallsCounter);

    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance);
    res.setLength(windowSize);
    res.setInfo("distance calls: " + distanceCallsCounter);
    return res;
  }

}
