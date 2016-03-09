package net.seninp.jmotif.sax.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

/**
 * Implements a parallel SAX factory class.
 *
 * @author psenin
 */
public class ParallelSAXImplementation {

  // locale, charset, etc
  static final int COMPLETED_FLAG = -1;

  // logging stuff
  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelSAXImplementation.class);

  private ExecutorService executorService;

  /**
   * Constructor.
   */
  public ParallelSAXImplementation() {
    super();
  }

  /**
   * Discretizes a time series using N threads. If interrupted returns null.
   *
   * @param timeseries the input time series.
   * @param threadsNum the number of threads to allocate for conversion.
   * @param slidingWindowSize the SAX sliding window size.
   * @param paaSize the SAX PAA size.
   * @param alphabetSize the SAX alphabet size.
   * @param numRedStrategy the SAX numerosity reduction strategy.
   * @param normalizationThreshold the normalization threshold.
   * @return a SAX representation of the input time series.
   * @throws SAXException if error occurs.
   */
  public SAXRecords process(double[] timeseries, int threadsNum, int slidingWindowSize, int paaSize,
      int alphabetSize, NumerosityReductionStrategy numRedStrategy, double normalizationThreshold)
      throws SAXException {

    LOGGER.debug("Starting the parallel SAX");

    NormalAlphabet na = new NormalAlphabet();

    SAXProcessor sp = new SAXProcessor();

    SAXRecords res = new SAXRecords(0);

    executorService = Executors.newFixedThreadPool(threadsNum);
    LOGGER.debug("Created thread pool of {} threads", threadsNum);

    NumerosityReductionStrategy nrStrategy = NumerosityReductionStrategy
        .fromValue(numRedStrategy.index());
    //
    // *** I can't figure out how to process MINDIST in parallel for now, rolling back onto failsafe
    // implementation
    //
    if (NumerosityReductionStrategy.MINDIST.equals(nrStrategy)) {
      nrStrategy = NumerosityReductionStrategy.NONE;
    }

    ExecutorCompletionService<HashMap<Integer, char[]>> completionService = new ExecutorCompletionService<HashMap<Integer, char[]>>(
        executorService);

    int totalTaskCounter = 0;

    // this value used as a job id in future
    //
    final long tstamp = System.currentTimeMillis();

    // first chunk takes on the uneven division
    //
    int evenIncrement = timeseries.length / threadsNum;
    if (evenIncrement <= slidingWindowSize) {
      LOGGER.warn("Unable to run with {} threads. Rolling back to single-threaded implementation.",
          threadsNum);
      return sp.ts2saxViaWindow(timeseries, slidingWindowSize, paaSize, na.getCuts(alphabetSize),
          nrStrategy, normalizationThreshold);
    }

    int reminder = timeseries.length % threadsNum;
    int firstChunkSize = evenIncrement + reminder;
    LOGGER.debug("data size {}, evenIncrement {}, reminder {}, firstChunkSize {}",
        timeseries.length, evenIncrement, reminder, firstChunkSize);

    // execute chunks processing
    //

    // the first chunk
    {
      int firstChunkStart = 0;
      int firstChunkEnd = (firstChunkSize - 1) + slidingWindowSize;
      final SAXWorker job0 = new SAXWorker(tstamp + totalTaskCounter, timeseries, firstChunkStart,
          firstChunkEnd, slidingWindowSize, paaSize, alphabetSize, nrStrategy,
          normalizationThreshold);
      completionService.submit(job0);
      LOGGER.debug("submitted first chunk job {}", tstamp);
      totalTaskCounter++;
    }

    // intermediate chunks
    while (totalTaskCounter < threadsNum - 1) {
      int intermediateChunkStart = (firstChunkSize - 1) + (totalTaskCounter - 1) * evenIncrement
          + 1;
      int intermediateChunkEnd = (firstChunkSize - 1) + (totalTaskCounter * evenIncrement)
          + slidingWindowSize;
      final SAXWorker job = new SAXWorker(tstamp + totalTaskCounter, timeseries,
          intermediateChunkStart, intermediateChunkEnd, slidingWindowSize, paaSize, alphabetSize,
          nrStrategy, normalizationThreshold);
      completionService.submit(job);
      LOGGER.debug("submitted intermediate chunk job {}", (tstamp + totalTaskCounter));
      totalTaskCounter++;
    }

    // the last chunk
    {
      int lastChunkStart = timeseries.length - evenIncrement;
      int lastChunkEnd = timeseries.length;
      final SAXWorker jobN = new SAXWorker(tstamp + totalTaskCounter, timeseries, lastChunkStart,
          lastChunkEnd, slidingWindowSize, paaSize, alphabetSize, nrStrategy,
          normalizationThreshold);
      completionService.submit(jobN);
      LOGGER.debug("submitted last chunk job {}", (tstamp + totalTaskCounter));
      totalTaskCounter++;
    }

    executorService.shutdown();

    // the array of completed tasks
    int[] completedChunks = new int[threadsNum];

    try {
      while (totalTaskCounter > 0) {

        if (Thread.currentThread().isInterrupted()) {
          LOGGER.info("Parallel SAX being interrupted, returning NULL!");
          return null;
        }

        Future<HashMap<Integer, char[]>> finished = completionService.poll(24, TimeUnit.HOURS);

        if (null == finished) {
          // something went wrong - break from here
          LOGGER.info("Breaking POLL loop after 24 HOURS of waiting...");
          break;
        }
        else {

          // get the result out
          //
          HashMap<Integer, char[]> chunkRes = finished.get();

          // ArrayList<Integer> keys = new ArrayList<Integer>();
          // for (int i : chunkRes.keySet()) {
          // keys.add(i);
          // }
          // Collections.sort(keys);
          // for (int i : keys) {
          // System.out.println(i + "," + String.valueOf(chunkRes.get(i)));
          // }

          // get the real job index out
          //
          int idx = (int) (Long.parseLong(String.valueOf(chunkRes.get(-1))) - tstamp);

          LOGGER.debug("job with stamp {} of chunk {} has finished", chunkRes.get(-1), idx);
          LOGGER.debug("current completion status: {} completion flag: {}",
              Arrays.toString(completedChunks), COMPLETED_FLAG);

          chunkRes.remove(-1);

          if (0 == res.size() || nrStrategy.equals(NumerosityReductionStrategy.NONE)) {
            res.addAll(chunkRes);
            completedChunks[idx] = COMPLETED_FLAG;
            if (nrStrategy.equals(NumerosityReductionStrategy.NONE)) {
              LOGGER.debug("merged in as is because the NR strategy is NONE");
            }
            else {
              LOGGER.debug("merged in as is because the result id empty");
            }
          }
          else {

            LOGGER.debug("processing chunk {}; res has results already...", idx);

            // the very first chunk has ID=0
            //
            if (0 == idx) {
              completedChunks[0] = COMPLETED_FLAG;

              if (completedChunks[1] == COMPLETED_FLAG) {

                LOGGER.debug("this is the very first chunk, merging the tail only");

                // chunk tail
                int chunkTailIndex = Collections.max(chunkRes.keySet());
                String tailStr = String.valueOf(chunkRes.get(chunkTailIndex));

                // res head
                int resultHeadIndex = res.getMinIndex();
                SAXRecord resultHead = res.getByIndex(resultHeadIndex);
                String headStr = String.valueOf(resultHead.getPayload());

                // print the log
                LOGGER.debug("first index in the res {} for {}, last index in head {} for {}",
                    resultHeadIndex, headStr, chunkTailIndex, headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && headStr.equalsIgnoreCase(tailStr)) {
                  LOGGER.debug("res head {} at {} is dropped in favor of head tail {} at {}",
                      headStr, resultHeadIndex, tailStr, chunkTailIndex);
                  res.dropByIndex(resultHeadIndex);
                }
                // else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                // && (sp.checkMinDistIsZero(tailStr.toCharArray(), headStr.toCharArray()))) {
                // LOGGER.debug("res head " + headStr + " at " + resultHeadIndex
                // + " is dropped in favor of head tail " + tailStr + " at " + chunkTailIndex);
                // res.dropByIndex(resultHeadIndex);
                //
                // }
              }
              else {
                LOGGER.debug(
                    "this is the very first chunk, but second is not yet in the results, merging all in");
              }
              res.addAll(chunkRes);
            }
            else if (threadsNum - 1 == idx) {
              completedChunks[idx] = COMPLETED_FLAG;

              if (completedChunks[idx - 1] == COMPLETED_FLAG) {

                LOGGER.debug("this is the very last chunk, merging the head only");

                int chunkHeadIndex = Collections.min(chunkRes.keySet());
                String headStr = String.valueOf(chunkRes.get(chunkHeadIndex));

                // find the RES last index
                int resultTailIndex = res.getMaxIndex();
                SAXRecord resTail = res.getByIndex(resultTailIndex);
                String resStr = String.valueOf(resTail.getPayload());

                LOGGER.debug("last index in the res {} for {}, first index in the tail {} for {}",
                    resultTailIndex, resStr, chunkHeadIndex, headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && resStr.equalsIgnoreCase(headStr)) {
                  LOGGER.debug("chunk head {} at {} is dropped in favor of res tail {} at {}",
                      headStr, chunkHeadIndex, resStr, resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
                // else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                // && (sp.checkMinDistIsZero(headStr.toCharArray(), resStr.toCharArray()))) {
                // LOGGER.debug("chunk head " + headStr + " at " + chunkHeadIndex
                // + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                // chunkRes.remove(chunkHeadIndex);
                // }
              }
              else {
                LOGGER.debug(
                    "this is the very last chunk, but previous is not yet in the results, merging all in");
              }
              res.addAll(chunkRes);
            }
            else {
              // the other chunks
              //
              completedChunks[idx] = COMPLETED_FLAG;

              LOGGER.debug("processing chunk {}", idx);

              if (completedChunks[idx - 1] == COMPLETED_FLAG) {

                LOGGER.debug("previous chunk was completed, merging in");

                int chunkHeadIndex = Collections.min(chunkRes.keySet());
                String headStr = String.valueOf(chunkRes.get(chunkHeadIndex));

                // find the RES last index
                int tmpIdx = chunkHeadIndex;
                while (null == res.getByIndex(tmpIdx)) {
                  tmpIdx--;
                }
                int resultTailIndex = tmpIdx;
                SAXRecord resTail = res.getByIndex(resultTailIndex);
                String resStr = String.valueOf(resTail.getPayload());

                LOGGER.debug("last index in the res {} for {}, first index in the chunk {} for {}",
                    resultTailIndex, resStr, chunkHeadIndex, headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && resStr.equalsIgnoreCase(headStr)) {
                  LOGGER.debug("chunk head {} at {} is dropped in favor of res tail {} at {}",
                      headStr, chunkHeadIndex, resStr, resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
                // else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                // && (sp.checkMinDistIsZero(headStr.toCharArray(), resStr.toCharArray()))) {
                // LOGGER.debug("chunk head " + headStr + " at " + chunkHeadIndex
                // + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                // chunkRes.remove(chunkHeadIndex);
                // }
              }

              if (completedChunks[idx + 1] == COMPLETED_FLAG) {

                LOGGER.debug("next chunk was completed, merging the tail");

                // chunk tail
                int chunkTailIdx = Collections.max(chunkRes.keySet());
                String tailStr = String.valueOf(chunkRes.get(chunkTailIdx));

                // res head
                int tmpIdx = chunkTailIdx;
                while (null == res.getByIndex(tmpIdx)) {
                  tmpIdx++;
                }
                int resultHeadIndex = tmpIdx;
                SAXRecord resultHead = res.getByIndex(resultHeadIndex);
                String headStr = String.valueOf(resultHead.getPayload());

                // print the log
                LOGGER.debug("last index in the res {} for {}, first index in the chunk {} for {}",
                    resultHeadIndex, headStr, chunkTailIdx, headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && headStr.equalsIgnoreCase(tailStr)) {
                  LOGGER.debug("chunk head {} at {} is dropped in favor of res tail {} at {}",
                      headStr, resultHeadIndex, tailStr, chunkTailIdx);
                  res.dropByIndex(resultHeadIndex);
                }
                // else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                // && (sp.checkMinDistIsZero(tailStr.toCharArray(), headStr.toCharArray()))) {
                // LOGGER.debug("res head " + headStr + " at " + resultHeadIndex
                // + " is dropped in favor of chunk tail " + tailStr + " at " + chunkTailIdx);
                // res.dropByIndex(resultHeadIndex);
                // }
              }

              res.addAll(chunkRes);

            }
          }
        }
        totalTaskCounter--;
      }
    }
    catch (InterruptedException e) {
      LOGGER.error("Error while waiting results.", e);
      this.cancel();
    }
    catch (Exception e) {
      LOGGER.error("Error while waiting results.", e);
    }
    finally {
      // wait at least 1 more hour before terminate and fail
      try {
        if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
          executorService.shutdownNow(); // Cancel currently executing tasks
          if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
            System.err.println("Pool did not terminate... FATAL ERROR");
            throw new RuntimeException("Parallel SAX pool did not terminate... FATAL ERROR");
          }
        }
      }
      catch (InterruptedException ie) {
        LOGGER.error("Error while waiting interrupting.", ie);
        // (Re-)Cancel if current thread also interrupted
        executorService.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }

    }

    if (NumerosityReductionStrategy.MINDIST.equals(numRedStrategy)) {

      // need to prune the result according to MINDIST strategy

      SAXRecords newRes = new SAXRecords();
      ArrayList<Integer> keys = res.getAllIndices();
      char[] oldStr = null;
      for (int i : keys) {

        SAXRecord entry = res.getByIndex(i);

        if (null != oldStr && sp.checkMinDistIsZero(entry.getPayload(), oldStr)) {
          continue;
        }

        newRes.add(entry.getPayload(), i);
        oldStr = entry.getPayload();

      }

      res = newRes;

    }

    return res;
  }

  /**
   * Cancels the execution.
   */
  public void cancel() {
    try {
      executorService.shutdown();
      if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
        executorService.shutdownNow(); // Cancel currently executing tasks
        if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
          LOGGER.error("Pool did not terminate... FATAL ERROR");
          throw new RuntimeException("Parallel SAX pool did not terminate... FATAL ERROR");
        }
      }
      else {
        LOGGER.error("Parallel SAX was interrupted by a request");
      }
    }
    catch (InterruptedException ie) {
      LOGGER.error("Error while waiting interrupting.", ie);
      // (Re-)Cancel if current thread also interrupted
      executorService.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
