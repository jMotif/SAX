package net.seninp.jmotif.sax.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.FrequencyTableEntry;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.registry.MagicArrayEntry;
import net.seninp.jmotif.sax.registry.SlidingWindowMarkerAlgorithm;
import net.seninp.jmotif.sax.registry.VisitRegistry;

/**
 * Implements HOTSAX discord discovery algorithm.
 *
 * @author psenin
 */
public class HOTSAXImplementation {

  private static TSProcessor tp = new TSProcessor();
  private static SAXProcessor sp = new SAXProcessor();
  private static EuclideanDistance ed = new EuclideanDistance();

  // static block - we instantiate the logger
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(HOTSAXImplementation.class);

  /**
   * Hash-table backed implementation (in contrast to trie). Time series is converted into a
   * SAXRecords data structure first, Hash-table backed magic array created second. HOTSAX applied
   * third. Nearest neighbors are searched only among the subsequences which were produced by SAX
   * with specified numerosity reduction. Thus, if the strategy is EXACT or MINDIST, discords do not
   * match those produced by BruteForce or NONE.
   *
   * @param series The timeseries.
   * @param discordsNumToReport The number of discords to report.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA value.
   * @param alphabetSize SAX alphabet size.
   * @param strategy the numerosity reduction strategy.
   * @param nThreshold the normalization threshold value.
   * @return The set of discords found within the time series, it may return less than asked for --
   * in this case, there are no more discords.
   * @throws Exception if error occurs., currentPos + windowSize
   */
  public static DiscordRecords series2Discords(double[] series, int discordsNumToReport,
      int windowSize, int paaSize, int alphabetSize, NumerosityReductionStrategy strategy,
      double nThreshold) throws Exception {

    // fix the start time
    Date start = new Date();

    // get the SAX transform done
    NormalAlphabet normalA = new NormalAlphabet();
    SAXRecords sax = sp.ts2saxViaWindow(series, windowSize, alphabetSize,
        normalA.getCuts(alphabetSize), strategy, nThreshold);
    Date saxEnd = new Date();
    LOGGER.debug("discretized in {}, words: {}, indexes: {}",
        SAXProcessor.timeToString(start.getTime(), saxEnd.getTime()), sax.getRecords().size(),
        sax.getIndexes().size());

    // fill the array for the outer loop
    ArrayList<MagicArrayEntry> magicArray = new ArrayList<MagicArrayEntry>(sax.getRecords().size());
    for (SAXRecord sr : sax.getRecords()) {
      magicArray.add(new MagicArrayEntry(String.valueOf(sr.getPayload()), sr.getIndexes().size()));
    }
    Date hashEnd = new Date();
    LOGGER.debug("Magic array filled in : {}",
        SAXProcessor.timeToString(saxEnd.getTime(), hashEnd.getTime()));

    DiscordRecords discords = getDiscordsWithMagic(series, sax, windowSize, magicArray,
        discordsNumToReport);

    Date end = new Date();

    LOGGER.debug("{} discords found in {}", discords.getSize(),
        SAXProcessor.timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  private static DiscordRecords getDiscordsWithMagic(double[] series, SAXRecords sax,
      int windowSize, ArrayList<MagicArrayEntry> magicArray, int discordCollectionSize)
      throws Exception {

    // sort the candidates
    Collections.sort(magicArray);

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry
    HashSet<Integer> visitRegistry = new HashSet<Integer>(windowSize * discordCollectionSize);

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      LOGGER.trace("currently known discords: {} out of {}", discords.getSize(),
          discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordWithMagic(series, windowSize, sax, magicArray,
          visitRegistry);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        LOGGER.trace("breaking the outer search loop, discords found: {} last seen discord: {}",
            discords.getSize(), bestDiscord);
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
      int markStart = bestDiscord.getPosition() - windowSize;
      // if (markStart < 0) {
      // markStart = 0;
      // }
      int markEnd = bestDiscord.getPosition() + windowSize;
      // if (markEnd > series.length) {
      // markEnd = series.length;
      // }
      LOGGER.debug("marking as globally visited [{}, {}]", markStart, markEnd);
      for (int i = markStart; i < markEnd; i++) {
        visitRegistry.add(i);
      }

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
   * @param sax The SAX data structure for the reference.
   * @param allWords The magic heuristics array.
   * @param discordRegistry The global visit array.
   * @return The best discord instance.
   * @throws Exception If error occurs.
   */
  private static DiscordRecord findBestDiscordWithMagic(double[] series, int windowSize,
      SAXRecords sax, ArrayList<MagicArrayEntry> allWords, HashSet<Integer> discordRegistry)
      throws Exception {

    // prepare the visits array, note that there can't be more points to visit that in a SAX index
    int[] visitArray = new int[series.length];

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarWord = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // System.err.println(frequencies.size() + " left to iterate over");
    LOGGER.debug("iterating over {} entries", allWords.size());

    for (MagicArrayEntry currentEntry : allWords) {

      // look into that entry
      String currentWord = currentEntry.getStr();
      Set<Integer> occurrences = sax.getByWord(currentWord).getIndexes();

      // we shall iterate over these candidate positions first
      for (int currentPos : occurrences) {

        iterationCounter++;

        // make sure it is not a previously found discord passed through the parameters array
        //
        // note, that the discordRegistry contains the whole span of previously found discord,
        // not just it's position....
        //
        //
        if (discordRegistry.contains(currentPos)) {
          continue;
        }

        LOGGER.trace("conducting search for {} at {}, iteration {}", currentWord, currentPos,
            iterationCounter);

        int markStart = currentPos - windowSize;
        // if (markStart < 0) {
        // markStart = 0;
        // }
        int markEnd = currentPos + windowSize;
        // if (markEnd > series.length) {
        // markEnd = series.length;
        // }

        // all the candidates we are not going to try
        HashSet<Integer> alreadyVisited = new HashSet<Integer>(
            occurrences.size() + (markEnd - markStart));

        for (int i = markStart; i < markEnd; i++) {
          alreadyVisited.add(i);
        }

        // fix the current subsequence trace
        double[] currentCandidateSeq = tp.subseriesByCopy(series, currentPos,
            currentPos + windowSize);

        // let the search begin ..
        double nearestNeighborDist = Double.MAX_VALUE;
        boolean doRandomSearch = true;

        for (Integer nextOccurrence : occurrences) {

          // just in case there is an overlap
          if (alreadyVisited.contains(nextOccurrence)) {
            continue;
          }
          else {
            alreadyVisited.add(nextOccurrence);
          }

          // get the subsequence and the distance
          // double[] occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence,
          // nextOccurrence + windowSize);
          // double dist = ed.distance(currentCandidateSeq, occurrenceSubsequence);
          double dist = distance(currentCandidateSeq, series, nextOccurrence,
              nextOccurrence + windowSize);
          distanceCalls++;

          // keep track of best so far distance
          if (dist < nearestNeighborDist) {
            nearestNeighborDist = dist;
            LOGGER.trace(" ** current NN at {}, distance: {}, pos {}", nextOccurrence,
                nearestNeighborDist, currentPos);
          }
          if (dist < bestSoFarDistance) {
            LOGGER.trace(
                " ** abandoning the occurrences loop, distance {} is less than the best so far {}",
                dist, bestSoFarDistance);
            doRandomSearch = false;
            break;
          }

        }

        // check if we must continue with random neighbors

        if (doRandomSearch) {
          LOGGER.trace("starting random search");

          // init the visit array
          //
          int visitCounter = 0;
          int cIndex = 0;
          for (int i = 0; i < series.length - windowSize; i++) {
            if (!(alreadyVisited.contains(i))) {
              visitArray[cIndex] = i;
              cIndex++;
            }
          }
          cIndex--;

          // shuffle the visit array
          //
          Random rnd = new Random();
          for (int i = cIndex; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = visitArray[index];
            visitArray[index] = visitArray[i];
            visitArray[i] = a;
          }

          // while there are unvisited locations
          while (cIndex >= 0) {

            int randomPos = visitArray[cIndex];
            cIndex--;

            // double[] randomSubsequence = tp.subseriesByCopy(series, randomPos,
            // randomPos + windowSize);
            // double dist = ed.distance(currentCandidateSeq, randomSubsequence);
            double dist = distance(currentCandidateSeq, series, randomPos, randomPos + windowSize);
            distanceCalls++;

            // keep track
            if (dist < nearestNeighborDist) {
              LOGGER.trace(" ** current NN at {}, distance: {}", +randomPos, dist);
              nearestNeighborDist = dist;
            }

            // early abandoning of the search:
            // the current word is not discord, we have seen better
            if (dist < bestSoFarDistance) {
              nearestNeighborDist = dist;
              LOGGER.trace(" ** abandoning random visits loop, seen distance {} at iteration {}",
                  nearestNeighborDist, visitCounter);

              break;
            }

            visitCounter = visitCounter + 1;

          } // while inner loop

        } // end of random search loop

        if (nearestNeighborDist > bestSoFarDistance && nearestNeighborDist < Double.MAX_VALUE) {
          LOGGER.debug("discord updated: pos {}, dist {}", currentPos, bestSoFarDistance);
          bestSoFarDistance = nearestNeighborDist;
          bestSoFarPosition = currentPos;
          bestSoFarWord = currentWord;
        }

        LOGGER.trace(" . . iterated {} times, best distance:  {} for a string {} at {}",
            iterationCounter, bestSoFarDistance, bestSoFarWord, bestSoFarPosition);

      } // outer loop inner part
    } // outer loop

    LOGGER.trace("Distance calls: {}", distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarWord);
    res.setInfo("distance calls: " + distanceCalls);

    return res;

  }

  /**
   * Old implementation. Nearest neighbot for a candidate subsequence is searched among all other
   * time-series subsequences, regardless of their exclusion by EXACT or MINDIST. This also accepts
   * a marker implementation which is responsible for marking a segment as visited.
   *
   * @param series The timeseries.
   * @param discordsNumToReport The number of discords to report.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA value.
   * @param alphabetSize SAX alphabet size.
   * @param markerAlgorithm marker algorithm.
   * @param strategy the numerosity reduction strategy.
   * @param nThreshold the normalization threshold value.
   * @return Discords found within the series.
   * @throws Exception if error occurs.
   */
  @Deprecated
  public static DiscordRecords series2DiscordsDeprecated(double[] series, int discordsNumToReport,
      int windowSize, int paaSize, int alphabetSize, SlidingWindowMarkerAlgorithm markerAlgorithm,
      NumerosityReductionStrategy strategy, double nThreshold) throws Exception {

    Date start = new Date();
    // get the SAX transform
    NormalAlphabet normalA = new NormalAlphabet();
    SAXRecords sax = sp.ts2saxViaWindow(series, windowSize, alphabetSize,
        normalA.getCuts(alphabetSize), strategy, nThreshold);
    Date saxEnd = new Date();
    LOGGER.debug("discretized in {}, words: {}, indexes: {}",
        SAXProcessor.timeToString(start.getTime(), saxEnd.getTime()), sax.getRecords().size(),
        sax.getIndexes().size());

    // instantiate the hash
    HashMap<String, ArrayList<Integer>> hash = new HashMap<String, ArrayList<Integer>>();

    // fill the hash
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
    LOGGER.debug("Hash filled in : {}",
        SAXProcessor.timeToString(saxEnd.getTime(), hashEnd.getTime()));

    DiscordRecords discords = getDiscordsWithHash(series, windowSize, hash, discordsNumToReport,
        markerAlgorithm);

    Date end = new Date();

    LOGGER.info("{} discords found in {}", discords.getSize(),
        SAXProcessor.timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  @Deprecated
  private static DiscordRecords getDiscordsWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash, int discordCollectionSize,
      SlidingWindowMarkerAlgorithm markerAlgorithm) throws Exception {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);
    globalTrackVisitRegistry.markVisited(series.length - windowSize, windowSize);

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      LOGGER.trace("currently known discords: {} out of {}", discords.getSize(),
          discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordWithHash(series, windowSize, hash,
          globalTrackVisitRegistry);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        LOGGER.trace("breaking the outer search loop, discords found: {} last seen discord: {}",
            discords.getSize(), bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo(
          "position " + bestDiscord.getPosition() + ", NN distance " + bestDiscord.getNNDistance()
              + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime())
              + ", " + bestDiscord.getInfo());
      LOGGER.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      markerAlgorithm.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

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
   */
  @Deprecated
  private static DiscordRecord findBestDiscordWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash, VisitRegistry globalRegistry) throws Exception {

    // we extract all seen words from the trie and sort them by the frequency decrease
    ArrayList<FrequencyTableEntry> frequencies = hashToFreqEntries(hash);
    Collections.sort(frequencies);

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarWord = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // System.err.println(frequencies.size() + " left to iterate over");

    while (!frequencies.isEmpty()) {

      iterationCounter++;

      // the head of this array has the rarest word
      FrequencyTableEntry currentEntry = frequencies.remove(0);
      // if (frequencies.size() % 10000 == 0) {
      // System.err.println(frequencies.size() + " left to iterate over");
      // }
      String currentWord = String.valueOf(currentEntry.getStr());
      int currentPos = currentEntry.getPosition();

      // make sure it is not previously found discord passed through the parameters array
      if (globalRegistry.isVisited(currentPos)) {
        continue;
      }

      // all the candidates we are going to try
      VisitRegistry randomRegistry = new VisitRegistry(series.length);
      randomRegistry.markVisited(series.length - windowSize, series.length);

      int markStart = currentPos - windowSize;
      if (markStart < 0) {
        markStart = 0;
      }
      int markEnd = currentPos + windowSize;
      if (markEnd > series.length) {
        markEnd = series.length;
      }
      randomRegistry.markVisited(markStart, markEnd);

      LOGGER.trace("conducting search for {} at {}, iteration {}, to go: {}", currentWord,
          currentPos, iterationCounter, frequencies.size());

      // fix the current subsequence trace
      double[] currentCandidateSeq = tp.subseriesByCopy(series, currentPos,
          currentPos + windowSize);

      // let the search begin ..
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURRENCES HERE FIRST
      List<Integer> currentWordOccurrences = hash.get(currentWord);

      for (Integer nextOccurrence : currentWordOccurrences) {

        // just in case there is an overlap
        if (randomRegistry.isVisited(nextOccurrence.intValue())) {
          continue;
        }
        else {
          randomRegistry.markVisited(nextOccurrence.intValue());
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence,
            nextOccurrence + windowSize);
        double dist = ed.distance(currentCandidateSeq, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          LOGGER.trace(" ** current NN at {}, distance: {}, pos {}" + nextOccurrence,
              nearestNeighborDist, currentPos);

        }
        if (dist < bestSoFarDistance) {
          LOGGER.trace(" ** abandoning random visits loop, seen distance {} at iteration {}", dist,
              bestSoFarDistance);

          doRandomSearch = false;
          break;
        }

      }

      // check if we must continue with random neighbors

      if (doRandomSearch) {
        LOGGER.trace("starting random search");

        int visitCounter = 0;

        // while there are unvisited locations
        int randomPos = -1;
        while (-1 != (randomPos = randomRegistry.getNextRandomUnvisitedPosition())) {

          randomRegistry.markVisited(randomPos);

          double[] randomSubsequence = tp.subseriesByCopy(series, randomPos,
              randomPos + windowSize);
          double dist = ed.distance(currentCandidateSeq, randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (dist < bestSoFarDistance) {
            nearestNeighborDist = dist;
            LOGGER.trace(" ** abandoning random visits loop, seen distance {} at iteration {}",
                nearestNeighborDist, visitCounter);
            break;
          }

          // keep track
          if (dist < nearestNeighborDist) {
            LOGGER.trace(" ** current NN at {}, distance: {}, pos {}" + randomPos, dist,
                currentPos);
            nearestNeighborDist = dist;
          }

          visitCounter = visitCounter + 1;

        } // while inner loop

      } // end of random search loop

      if (nearestNeighborDist > bestSoFarDistance) {
        LOGGER.debug("discord updated: pos {}, dist {}", currentPos, bestSoFarDistance);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = currentPos;
        bestSoFarWord = currentWord;
      }

      LOGGER.trace(" . . iterated {} times, best distance: {} for a string {} at {}",
          iterationCounter, bestSoFarDistance, bestSoFarWord, bestSoFarPosition);

    } // outer loop

    LOGGER.trace("Distance calls: {}", distanceCalls);
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
  @Deprecated
  private static ArrayList<FrequencyTableEntry> hashToFreqEntries(
      HashMap<String, ArrayList<Integer>> hash) {
    ArrayList<FrequencyTableEntry> res = new ArrayList<FrequencyTableEntry>();
    for (Entry<String, ArrayList<Integer>> e : hash.entrySet()) {
      char[] payload = e.getKey().toCharArray();
      int frequency = e.getValue().size();
      for (Integer i : e.getValue()) {
        res.add(new FrequencyTableEntry(i, payload.clone(), frequency));
      }
    }
    return res;
  }

  /**
   * Calculates the Euclidean distance between two points. Don't use this unless you need that.
   * 
   * @param subseries The first point.
   * @param series The second point.
   * @param from the initial index of the range to be copied, inclusive
   * @param to the final index of the range to be copied, exclusive. (This index may lie outside the
   * array.)
   * @return The Euclidean distance.
   */
  private static double distance(double[] subseries, double[] series, int from, int to)
      throws Exception {
    Double sum = 0D;
    for (int i = from; i < to; i++) {
      double tmp = subseries[i - from] - series[i];
      sum = sum + tmp * tmp;
    }
    return Math.sqrt(sum);
  }

}
