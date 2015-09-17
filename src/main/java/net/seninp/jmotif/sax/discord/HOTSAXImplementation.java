package net.seninp.jmotif.sax.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecord;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.registry.SlidingWindowMarkerAlgorithm;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import net.seninp.jmotif.sax.trie.SAXTrie;
import net.seninp.jmotif.sax.trie.SAXTrieHitEntry;
import net.seninp.jmotif.sax.trie.TrieException;

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

  private static final Level LOGGING_LEVEL = Level.DEBUG;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(HOTSAXImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Build the SAX trie out of the series and reports discords.
   * 
   * @param series The timeseries.
   * @param discordsNumToReport how many discords to report.
   * @param windowSize sliding window size to use.
   * @param alphabetSize The SAX alphabet size.
   * @param markerAlgorithm marker algorithm.
   * @param strategy numerosity reduction strategy.
   * @param nThreshold the normalization threshold value.
   * @return Discords found within the series.
   * @throws Exception if error occurs.
   */
  public static DiscordRecords series2Discords(double[] series, int discordsNumToReport,
      int windowSize, int alphabetSize, SlidingWindowMarkerAlgorithm markerAlgorithm,
      NumerosityReductionStrategy strategy, double nThreshold) throws Exception {

    Date start = new Date();
    // instantiate the trie
    SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);
    Date trieInitEnd = new Date();

    consoleLogger.debug(
        "Trie built in : " + SAXProcessor.timeToString(start.getTime(), trieInitEnd.getTime()));

    // get the SAX transform
    NormalAlphabet normalA = new NormalAlphabet();
    SAXRecords sax = sp.ts2saxViaWindow(series, windowSize, alphabetSize,
        normalA.getCuts(alphabetSize), strategy, nThreshold);
    Date saxEnd = new Date();
    consoleLogger.debug("Time series discretized in : "
        + SAXProcessor.timeToString(trieInitEnd.getTime(), saxEnd.getTime()));

    // fill the trie
    for (SAXRecord sr : sax.getRecords()) {
      for (Integer pos : sr.getIndexes()) {
        trie.put(String.valueOf(sr.getPayload()), pos);
      }
    }
    Date trieEnd = new Date();
    consoleLogger.debug(
        "Trie filled in : " + SAXProcessor.timeToString(saxEnd.getTime(), trieEnd.getTime()));

    int reportNum = DEFAULT_COLLECTION_SIZE;
    if (discordsNumToReport > 0 && discordsNumToReport < DEFAULT_COLLECTION_SIZE) {
      reportNum = discordsNumToReport;
    }

    DiscordRecords discords = getDiscords(series, windowSize, trie, reportNum, markerAlgorithm);

    Date end = new Date();

    consoleLogger.debug("trie-based discords search finished in : "
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
      int discordCollectionSize, SlidingWindowMarkerAlgorithm marker)
          throws Exception, TrieException {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);
    globalTrackVisitRegistry.markVisited(series.length - windowSize, series.length);

    // we conduct the search until the number of discords is less than desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.debug(
          "currently known discords: " + discords.getSize() + " out of " + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscord(series, windowSize, trie,
          globalTrackVisitRegistry, marker);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.debug("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo(
          "position " + bestDiscord.getPosition() + ", NN distance " + bestDiscord.getNNDistance()
              + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime())
              + ", " + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

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
   * @param globalRegistry The magic array.
   * @return The best discord instance.
   * @throws Exception If error occurs.
   * @throws TrieException If error occurs.
   */
  private static DiscordRecord findBestDiscord(double[] series, int windowSize, SAXTrie trie,
      VisitRegistry globalRegistry, SlidingWindowMarkerAlgorithm marker)
          throws Exception, TrieException {

    // we extract all seen words from the trie and sort them by the frequency decrease
    //
    ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();
    Collections.sort(frequencies);
    consoleLogger.debug("trie-based HOTSAX, iterating over " + frequencies.size() + " records");

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0;
    String bestSoFarWord = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // while not all sequences are considered
    VisitRegistry localRegistry = globalRegistry.clone();

    while (!frequencies.isEmpty()) {

      iterationCounter++;

      // the head of frequencies has the rarest word
      SAXTrieHitEntry currentEntry = frequencies.remove(0);
      String currentWord = String.valueOf(currentEntry.getStr());
      int currentPos = currentEntry.getPosition();

      // make sure it is not previously found discord passed through the parameters array
      if (globalRegistry.isVisited(currentPos)) {
        continue;
      }
      else {
        localRegistry.markVisited(currentPos);
      }

      consoleLogger.trace("conducting search for " + currentWord + " at " + currentPos
          + ", iteration " + iterationCounter + ", to go: " + frequencies.size());

      // fix the current subsequence
      double[] currentCandidateSeq = tp.subseriesByCopy(series, currentPos,
          currentPos + windowSize);

      // let the search begin ..
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE FIRST
      List<Integer> currentWordOccurences = trie.getOccurences(currentWord.toCharArray());

      for (Integer nextOccurrence : currentWordOccurences) {

        // just in case there is an overlap
        if (Math.abs(nextOccurrence.intValue() - currentPos) <= windowSize) {
          continue;
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence,
            nextOccurrence + windowSize);
        double dist = ed.distance(currentCandidateSeq, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger
              .trace(" ** current NN at " + nextOccurrence + ", distance: " + nearestNeighborDist);
        }
        if (dist < bestSoFarDistance) {
          consoleLogger.debug(" ** abandoning the occurrences loop, distance " + dist
              + " is less than best so far " + bestSoFarDistance);
          doRandomSearch = false;
          break;
        }

      }

      // consoleLogger.debug("occurrence loop finished");

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        consoleLogger.trace("starting random search");

        int visitCounter = 0;

        // while there are unvisited locations
        VisitRegistry randomRegistry = new VisitRegistry(series.length - windowSize);
        int randomPos = -1;
        while (-1 != (randomPos = randomRegistry.getNextRandomUnvisitedPosition())) {

          randomRegistry.markVisited(randomPos);

          if (Math.abs(currentPos - randomPos) <= windowSize) {
            continue;
          }

          double[] randomSubsequence = tp.subseriesByCopy(series, randomPos,
              randomPos + windowSize);
          double dist = ed.distance(currentCandidateSeq, randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (dist < bestSoFarDistance) {
            nearestNeighborDist = dist;
            consoleLogger.trace(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (dist < nearestNeighborDist) {
            consoleLogger.trace(" ** current NN at " + randomPos + ", distance: " + dist);
            nearestNeighborDist = dist;
          }

          visitCounter = visitCounter + 1;

        } // while inner loop

      } // end of random search loop

      if (nearestNeighborDist > bestSoFarDistance) {
        consoleLogger.debug("discord updated: pos " + currentPos + ", dist " + bestSoFarDistance);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = currentPos;
        bestSoFarWord = currentWord;
      }

      consoleLogger.trace(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a string " + bestSoFarWord + " at " + bestSoFarPosition);

    } // outer loop

    consoleLogger.trace("Distance calls: " + distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarWord);
    res.setInfo("distance calls: " + distanceCalls);
    return res;
  }

  /**
   * Build the SAX trie out of the series and reports discords. This uses default numerosity
   * reduction that is EXACT.
   * 
   * @param series The timeseries.
   * @param discordsNumToReport how many discords to report.
   * @param windowSize sliding window size to use.
   * @param paaSize PAA value to use.
   * @param alphabetSize The SAX alphabet size.
   * @param markerAlgorithm marker algorithm.
   * @param strategy numerosity reduction strategy.
   * @param nThreshold the normalization threshold value.
   * @return Discords found within the series.
   * @throws Exception if error occurs.
   */
  public static DiscordRecords series2DiscordsWithHash(double[] series, int discordsNumToReport,
      int windowSize, int paaSize, int alphabetSize, SlidingWindowMarkerAlgorithm markerAlgorithm,
      NumerosityReductionStrategy strategy, double nThreshold) throws Exception {

    Date start = new Date();
    // get the SAX transform
    NormalAlphabet normalA = new NormalAlphabet();
    SAXRecords sax = sp.ts2saxViaWindow(series, windowSize, alphabetSize,
        normalA.getCuts(alphabetSize), strategy, nThreshold);
    Date saxEnd = new Date();
    consoleLogger.debug("Time series discretized in : "
        + SAXProcessor.timeToString(start.getTime(), saxEnd.getTime()));

    // instantiate the hash
    HashMap<String, ArrayList<Integer>> hash = new HashMap<String, ArrayList<Integer>>();

    // fill the trie
    for (SAXRecord sr : sax.getRecords()) {
      for (Integer pos : sr.getIndexes()) {
        // add to hash
        String word = String.valueOf(sr.getPayload());
        if (!(hash.containsKey(word))) {
          hash.put(word, new ArrayList<Integer>());
        }
        hash.get(String.valueOf(word)).add(pos);
      }
    }
    Date hashEnd = new Date();
    consoleLogger.debug(
        "Hash filled in : " + SAXProcessor.timeToString(saxEnd.getTime(), hashEnd.getTime()));

    int reportNum = DEFAULT_COLLECTION_SIZE;
    if (discordsNumToReport > 0 && discordsNumToReport < 50) {
      reportNum = discordsNumToReport;
    }

    DiscordRecords discords = getDiscordsWithHash(series, windowSize, hash, reportNum,
        markerAlgorithm);

    Date end = new Date();

    consoleLogger.debug("hash-based discords search finished in : "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  private static DiscordRecords getDiscordsWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash, int discordCollectionSize,
      SlidingWindowMarkerAlgorithm markerAlgorithm) throws Exception {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);
    globalTrackVisitRegistry.markVisited(series.length - windowSize, series.length);

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.trace(
          "currently known discords: " + discords.getSize() + " out of " + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordWithHash(series, windowSize, hash,
          globalTrackVisitRegistry, markerAlgorithm);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.trace("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo(
          "position " + bestDiscord.getPosition() + ", NN distance " + bestDiscord.getNNDistance()
              + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime())
              + ", " + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      markerAlgorithm.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

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
   * @param hash The hash-based magic array.
   * @param globalRegistry The magic array.
   * @return The best discord instance.
   * @throws Exception If error occurs.
   * @throws TrieException If error occurs.
   */
  private static DiscordRecord findBestDiscordWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash, VisitRegistry globalRegistry,
      SlidingWindowMarkerAlgorithm marker) throws Exception {

    // we extract all seen words from the trie and sort them by the frequency decrease
    ArrayList<SAXTrieHitEntry> frequencies = hashToFrequencies(hash);
    Collections.sort(frequencies);

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarWord = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // while not all sequences are considered
    VisitRegistry localRegistry = globalRegistry.clone();

    System.err.println(frequencies.size() + " left to iterate over");

    while (!frequencies.isEmpty()) {

      iterationCounter++;

      // the head of this array has the rarest word
      SAXTrieHitEntry currentEntry = frequencies.remove(0);
      // if (frequencies.size() % 10000 == 0) {
      System.err.println(frequencies.size() + " left to iterate over");
      // }
      String currentWord = String.valueOf(currentEntry.getStr());
      int currentPos = currentEntry.getPosition();

      // make sure it is not previously found discord passed through the parameters array
      if (globalRegistry.isVisited(currentPos)) {
        continue;
      }
      else {
        localRegistry.markVisited(currentPos);
      }

      consoleLogger.trace("conducting search for " + currentWord + " at " + currentPos
          + ", iteration " + iterationCounter + ", to go: " + frequencies.size());

      // fix the current subsequencetrace
      double[] currentCandidateSeq = tp.subseriesByCopy(series, currentPos,
          currentPos + windowSize);

      // let the search begin ..
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE FIRST
      List<Integer> currentWordOccurences = hash.get(currentWord);

      for (Integer nextOccurrence : currentWordOccurences) {

        // just in case there is an overlap
        if (Math.abs(nextOccurrence.intValue() - currentPos) <= windowSize) {
          continue;
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence,
            nextOccurrence + windowSize);
        double dist = ed.distance(currentCandidateSeq, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.debug(" ** current NN at " + nextOccurrence + ", distance: "
              + nearestNeighborDist + ", pos " + currentPos);
        }
        if (dist < bestSoFarDistance) {
          consoleLogger.trace(" ** abandoning the occurrences loop, distance " + dist
              + " is less than best so far " + bestSoFarDistance);
          doRandomSearch = false;
          break;
        }

      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        consoleLogger.trace("starting random search");

        int visitCounter = 0;

        // while there are unvisited locations
        VisitRegistry randomRegistry = new VisitRegistry(series.length - windowSize);
        int randomPos = -1;
        while (-1 != (randomPos = randomRegistry.getNextRandomUnvisitedPosition())) {

          randomRegistry.markVisited(randomPos);

          if (Math.abs(currentPos - randomPos) <= windowSize) {
            continue;
          }

          double[] randomSubsequence = tp.subseriesByCopy(series, randomPos,
              randomPos + windowSize);
          double dist = ed.distance(currentCandidateSeq, randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (dist < bestSoFarDistance) {
            nearestNeighborDist = dist;
            consoleLogger.trace(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (dist < nearestNeighborDist) {
            consoleLogger.debug(
                " ** current NN at " + randomPos + ", distance: " + dist + ", pos " + currentPos);
            nearestNeighborDist = dist;
          }

          visitCounter = visitCounter + 1;

        } // while inner loop

      } // end of random search loop

      if (nearestNeighborDist > bestSoFarDistance) {
        consoleLogger.debug("discord updated: pos " + currentPos + ", dist " + bestSoFarDistance);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = currentPos;
        bestSoFarWord = currentWord;
      }

      consoleLogger.trace(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a string " + bestSoFarWord + " at " + bestSoFarPosition);

    } // outer loop

    consoleLogger.trace("Distance calls: " + distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarWord);
    res.setInfo("distance calls: " + distanceCalls);

    return res;

  }

  /**
   * Translates the hash table into sortable array of substrings.
   * 
   * @param hash
   * @return
   */
  private static ArrayList<SAXTrieHitEntry> hashToFrequencies(
      HashMap<String, ArrayList<Integer>> hash) {
    ArrayList<SAXTrieHitEntry> res = new ArrayList<SAXTrieHitEntry>();
    for (Entry<String, ArrayList<Integer>> e : hash.entrySet()) {
      char[] payload = e.getKey().toCharArray();
      int frequency = e.getValue().size();
      for (Integer i : e.getValue()) {
        res.add(new SAXTrieHitEntry(i, payload.clone(), frequency));
      }
    }
    return res;
  }
}
