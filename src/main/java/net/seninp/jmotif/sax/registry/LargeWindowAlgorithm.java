package net.seninp.jmotif.sax.registry;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Implements a marker for a visit registry. This implementation marks as visited starting from a
 * given position up to interval length -1 first. Then it marks the same interval to left, and the
 * same to the right. The idea is that none of new examined discords should not overlap with the
 * current one.
 * 
 * @author psenin
 * 
 */
public class LargeWindowAlgorithm implements SlidingWindowMarkerAlgorithm {

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(LargeWindowAlgorithm.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  @Override
  public void markVisited(VisitRegistry registry, int startPosition, int intervalLength) {

    consoleLogger
        .debug("marking as visited interval [" + String.valueOf(startPosition - intervalLength)
            + ", " + String.valueOf(startPosition + intervalLength) + ")");

    // mark the interval, this shall fit into the registry
    registry.markVisited(startPosition, startPosition + intervalLength);

    // grow left
    for (int i = 0; i <= intervalLength; i++) {
      if (startPosition - i < 0) {
        break;
      }
      registry.markVisited(startPosition - i);
    }

  }

}
