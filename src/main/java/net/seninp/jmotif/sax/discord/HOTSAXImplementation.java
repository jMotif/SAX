package net.seninp.jmotif.sax.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.algorithm.SlidingWindowMarkerAlgorithm;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import net.seninp.jmotif.sax.trie.SAXTrie;
import net.seninp.jmotif.sax.trie.SAXTrieHitEntry;
import net.seninp.jmotif.sax.trie.TrieException;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Implements HOTSAX discord discovery algorithm.
 * 
 * @author psenin
 * 
 */
public class HOTSAXImplementation {

  private static final int DEFAULT_COLLECTION_SIZE = 10;

  private static TSProcessor tp = new TSProcessor();
  private static SAXProcessor sp = new SAXProcessor();
  private static EuclideanDistance ed = new EuclideanDistance();

  // static block - we instantiate the logger
  //
  private static Logger consoleLogger;

  private static final Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(HOTSAXImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Build the SAX trie out of the series and reports discords.
   * 
   * @param series The timeseries.
   * @param windowSize PAA window size to use.
   * @param alphabetSize The SAX alphabet size.
   * @param discordsNumToReport how many discords to report.
   * @param nThreshold the normalization threshold value.
   * @return Discords found within the series.
   * @throws TrieException if error occurs.
   * @throws Exception if error occurs.
   */
  public static DiscordRecords series2Discords(double[] series, int windowSize, int alphabetSize,
      int discordsNumToReport, SlidingWindowMarkerAlgorithm markerAlgorithm, double nThreshold)
      throws TrieException, Exception {

    // get the Alphabet
    //
    NormalAlphabet normalA = new NormalAlphabet();

    Date start = new Date();
    // instantiate the trie
    //
    SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);
    Date trieInitEnd = new Date();

    consoleLogger.info("Trie built in : "
        + SAXProcessor.timeToString(start.getTime(), trieInitEnd.getTime()));

    // fill the trie with data
    //
    int currPosition = 0;
    while ((currPosition + windowSize) < series.length) {

      // get the subsequence
      double[] subSeries = tp.subseriesByCopy(series, currPosition, currPosition + windowSize);

      // convert to string
      char[] saxVals = sp.ts2string(subSeries, alphabetSize, normalA.getCuts(alphabetSize),
          nThreshold);

      // add to trie
      trie.put(String.valueOf(saxVals), currPosition);

      // increment the position
      currPosition++;
    }

    Date trieEnd = new Date();
    consoleLogger.debug("Time series processed in : "
        + SAXProcessor.timeToString(trieInitEnd.getTime(), trieEnd.getTime()));

    int reportNum = DEFAULT_COLLECTION_SIZE;
    if (discordsNumToReport > 0 && discordsNumToReport < DEFAULT_COLLECTION_SIZE) {
      reportNum = discordsNumToReport;
    }

    DiscordRecords discords = getDiscords(series, windowSize, trie, reportNum, markerAlgorithm);

    Date end = new Date();

    consoleLogger.debug("discords search finished in : "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  /**
   * The discords extraction method.
   * 
   * Here I need to keep a continuous stack of knowledge with information not only about distance,
   * but about abandoning or conducting a full search for words. Thus, I will not be doing the same
   * expensive search on the rarest word all over again.
   * 
   * @param series The series we work with.
   * @param windowSize The series window size.
   * @param marker The algorithm for marking visited locations.
   * @param trie
   * @param discordCollectionSize
   * @return
   * @throws Exception
   * @throws TrieException
   */
  private static DiscordRecords getDiscords(double[] series, int windowSize, SAXTrie trie,
      int discordCollectionSize, SlidingWindowMarkerAlgorithm marker) throws Exception,
      TrieException {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);

    // the collection of seen words and their best so far distances
    // in the collection, in addition to pairs <word, distance> I store a
    // semaphore which indicates whether the full search was conducted with this word,
    // or it was abandoned at some point, so we do not know the true near neighbor
    //
    TreeMap<String, DistanceEntry> knownWordsAndTheirCurrentDistances = new TreeMap<String, DistanceEntry>();

    // the words already in the discords collection, so we do not have to
    // re-consider them
    //
    // TreeSet<String> completeWords = new TreeSet<String>();

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.debug("currently known discords: " + discords.getSize() + " out of "
          + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscord(series, windowSize, trie,
          knownWordsAndTheirCurrentDistances, globalTrackVisitRegistry, marker);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.debug("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", NN distance "
          + bestDiscord.getNNDistance() + ", elapsed time: "
          + SAXProcessor.timeToString(start.getTime(), end.getTime()) + ", "
          + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

      // completeWords.add(String.valueOf(bestDiscord.getPayload()));
    }

    // done deal
    //
    return discords;
  }

  /**
   * This method reports the best found discord. Note, that this discord is approximately the best.
   * Due to the fuzzy-logic search with randomization and aggressive labeling of the magic array
   * locations.
   * 
   * @param series The series we are looking for discord in.
   * @param windowSize The sliding window size.
   * @param trie The trie (index of the series).
   * @param knownWordsAndTheirDistances The best known distances for certain word. I use the early
   * search abandoning optimization in oder to reduce complexity.
   * @param visitedLocations The magic array.
   * @return The best discord instance.
   * @throws Exception If error occurs.
   * @throws TrieException If error occurs.
   */
  private static DiscordRecord findBestDiscord(double[] series, int windowSize, SAXTrie trie,
      TreeMap<String, DistanceEntry> knownWordsAndTheirDistances, VisitRegistry visitedLocations,
      SlidingWindowMarkerAlgorithm marker) throws Exception, TrieException {

    // we extract all seen words from the trie and sort them by the frequency decrease
    ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();
    Collections.sort(frequencies);

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarString = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // while not all sequences are considered
    while (!frequencies.isEmpty()) {
      iterationCounter++;

      // the head of this array has the rarest word
      SAXTrieHitEntry currentEntry = frequencies.get(0);
      String currentWord = String.valueOf(currentEntry.getStr());
      int outerLoopCandidatePosition = currentEntry.getPosition();

      frequencies.remove(0);
      currentEntry = null;

      // make sure it is not previously found discord passed through the parameters array
      if (visitedLocations.isVisited(outerLoopCandidatePosition, outerLoopCandidatePosition
          + windowSize)) {
        continue;
      }

      // *** THIS IS AN OUTER LOOP - over the current motif
      consoleLogger.debug("conducting search for " + currentWord + " at "
          + outerLoopCandidatePosition + ", iteration " + iterationCounter + ", to go: "
          + frequencies.size());

      // let the search begin
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // get a copy of global restrictions on visited locations
      VisitRegistry registry = new VisitRegistry(series.length - windowSize);

      // extract the subsequence & mark visited current substring
      double[] currentSubsequence = tp.subseriesByCopy(series, outerLoopCandidatePosition,
          outerLoopCandidatePosition + windowSize);
      marker.markVisited(registry, outerLoopCandidatePosition, windowSize);

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
      List<Integer> currentOccurences = trie.getOccurences(currentWord.toCharArray());
      consoleLogger.debug(currentWord + " has " + currentOccurences.size()
          + " occurrences, iterating...");

      for (Integer nextOccurrence : currentOccurences) {

        // check this subsequence as visited
        registry.markVisited(nextOccurrence);

        // just in case there is an overlap
        if (Math.abs(nextOccurrence.intValue() - outerLoopCandidatePosition) <= windowSize) {
          continue;
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence, nextOccurrence
            + windowSize);
        double dist = ed.distance(currentSubsequence, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.debug(" ** current NN at " + nextOccurrence + ", distance: "
              + nearestNeighborDist);
          if (dist < bestSoFarDistance) {
            consoleLogger.debug(" ** abandoning the occurrences loop, distance " + dist
                + " is less than best so far " + bestSoFarDistance);
            doRandomSearch = false;
            break;
          }
        }
      }

      if (!(Double.isInfinite(nearestNeighborDist))) {
        consoleLogger.debug("for " + currentWord
            + " occurrences, smallest nearest neighbor distance: " + nearestNeighborDist);
      }
      else {
        consoleLogger.debug("nothing changed after iterations over current word positions ...");
      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        // it is heuristics here
        //
        int nextRandomSubsequencePosition = -1;

        int visitCounter = 0;

        // while there are unvisited locations
        while ((nextRandomSubsequencePosition = registry.getNextRandomUnvisitedPosition()) != -1) {
          registry.markVisited(nextRandomSubsequencePosition);

          double[] randomSubsequence = tp.subseriesByCopy(series, nextRandomSubsequencePosition,
              nextRandomSubsequencePosition + windowSize);
          double randomSubsequenceDistance = ed.distance(currentSubsequence, randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (randomSubsequenceDistance < bestSoFarDistance) {
            nearestNeighborDist = randomSubsequenceDistance;
            consoleLogger.debug(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (randomSubsequenceDistance < nearestNeighborDist) {
            nearestNeighborDist = randomSubsequenceDistance;
          }

          visitCounter = visitCounter + 1;
        } // while inner loop
        consoleLogger.debug("random visits loop finished, total positions considered: "
            + visitCounter);

      } // if break loop

      if (nearestNeighborDist > bestSoFarDistance) {
        consoleLogger.debug("beat best so far distance, updating from " + bestSoFarDistance
            + " to  " + nearestNeighborDist);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = outerLoopCandidatePosition;
        bestSoFarString = currentWord;
      }

      consoleLogger.debug(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a string " + bestSoFarString + " at " + bestSoFarPosition);

    } // outer loop

    consoleLogger.debug("Distance calls: " + distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarString);
    res.setInfo("distance calls: " + distanceCalls);
    return res;
  }

}
