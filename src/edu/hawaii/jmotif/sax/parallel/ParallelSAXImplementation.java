package edu.hawaii.jmotif.sax.parallel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.sax.SAXProcessor;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.sax.datastructures.SaxRecord;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Implements a parallel SAX factory class.
 * 
 * @author psenin
 * 
 */
public class ParallelSAXImplementation {

  // locale, charset, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  final static int COMPLETED_FLAG = -1;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(ParallelSAXImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public ParallelSAXImplementation() {
    super();
  }

  /**
   * Discretizes a time series using N threads.
   * 
   * @param timeseries the input time series.
   * @param threadsNum the number of threads to allocate for conversion.
   * @param slidingWindowSize the SAX sliding window size.
   * @param paaSize the SAX PAA size.
   * @param alphabetSize the SAX alphabet size.
   * @param nrStrategy the SAX numerosity reduction strategy.
   * @param normalizationThreshold the normalization threshold.
   * @return a SAX representation of the input time series.
   * @throws SAXException if error occurs.
   */
  public SAXRecords process(double[] timeseries, int threadsNum, int slidingWindowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy nrStrategy,
      double normalizationThreshold) throws SAXException {

    consoleLogger.debug("Starting the parallel SAX");

    NormalAlphabet na = new NormalAlphabet();

    SAXProcessor sp = new SAXProcessor();

    SAXRecords res = new SAXRecords(0);

    ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
    consoleLogger.debug("Created thread pool of " + threadsNum + " threads");

    CompletionService<HashMap<Integer, char[]>> completionService = new ExecutorCompletionService<HashMap<Integer, char[]>>(
        executorService);

    int totalTaskCounter = 0;

    // this value used as a job id in future
    //
    final long tstamp = System.currentTimeMillis();

    // first chunk takes on the uneven division
    //
    int evenIncrement = timeseries.length / threadsNum;
    if (evenIncrement <= slidingWindowSize) {
      consoleLogger.warn("Unable to run with " + threadsNum
          + " threads. Rolling back to single-threaded implementation.");
      return sp.ts2saxViaWindow(timeseries, slidingWindowSize, paaSize, na.getCuts(alphabetSize),
          nrStrategy, normalizationThreshold);
    }

    int reminder = timeseries.length % threadsNum;
    int firstChunkSize = evenIncrement + reminder;
    consoleLogger.debug("data size " + timeseries.length + ", evenIncrement " + evenIncrement
        + ", reminder " + reminder + ", firstChunkSize " + firstChunkSize);

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
      consoleLogger.debug("submitted first chunk job " + tstamp);
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
      consoleLogger.debug("submitted intermediate chunk job "
          + Long.valueOf(tstamp + totalTaskCounter));
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
      consoleLogger.debug("submitted last chunk job " + Long.valueOf(tstamp + totalTaskCounter));
      totalTaskCounter++;
    }

    executorService.shutdown();

    // the array of completed tasks
    int[] completedChunks = new int[threadsNum];

    try {
      while (totalTaskCounter > 0) {

        Future<HashMap<Integer, char[]>> finished = completionService.poll(128, TimeUnit.HOURS);

        if (null == finished) {
          // something went wrong - break from here
          System.err.println("Breaking POLL loop after 128 HOURS of waiting...");
          break;
        }
        else {

          // get the result out
          //
          HashMap<Integer, char[]> chunkRes = finished.get();

          // get the real job index out
          //
          int idx = (int) (Long.valueOf(String.valueOf(chunkRes.get(-1))) - tstamp);

          consoleLogger.debug("job with stamp " + String.valueOf(chunkRes.get(-1)) + " of chunk "
              + idx + " has finished");
          consoleLogger.debug("current completion status: " + Arrays.toString(completedChunks)
              + " completion flag: " + COMPLETED_FLAG);

          chunkRes.remove(-1);
          
          if (0 == res.size() || nrStrategy.equals(NumerosityReductionStrategy.NONE)) {
            res.addAll(chunkRes);
            completedChunks[idx] = COMPLETED_FLAG;
            if (nrStrategy.equals(NumerosityReductionStrategy.NONE)) {
              consoleLogger.debug("merged in as is because the NR strategy is NONE");
            }
            else {
              consoleLogger.debug("merged in as is because the result id empty");
            }
          }
          else {

            consoleLogger.debug("processing chunk " + idx + "; res has results already...");

            // the very first chunk has ID=0
            //
            if (0 == idx) {
              completedChunks[0] = COMPLETED_FLAG;

              if (completedChunks[1] == COMPLETED_FLAG) {

                consoleLogger.debug("this is the very first chunk, merging the tail only");

                // chunk tail
                int chunkTailIndex = Collections.max(chunkRes.keySet());
                String tailStr = String.valueOf(chunkRes.get(chunkTailIndex));

                // res head
                int resultHeadIndex = res.getMinIndex();
                SaxRecord resultHead = res.getByIndex(resultHeadIndex);
                String headStr = String.valueOf(resultHead.getPayload());

                // print the log
                consoleLogger.debug("first index in the res " + resultHeadIndex + " for " + headStr
                    + ", last index in head " + chunkTailIndex + " for " + headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && headStr.equalsIgnoreCase(tailStr)) {
                  consoleLogger.debug("res head " + headStr + " at " + resultHeadIndex
                      + " is dropped in favor of head tail " + tailStr + " at " + chunkTailIndex);
                  res.dropByIndex(resultHeadIndex);
                }
                else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                    && (sp.checkMinDistIsZero(tailStr.toCharArray(), resultHead.getPayload()))) {
                  consoleLogger.debug("res head " + headStr + " at " + resultHeadIndex
                      + " is dropped in favor of head tail " + tailStr + " at " + chunkTailIndex);
                  res.dropByIndex(resultHeadIndex);

                }
              }
              else {
                consoleLogger
                    .debug("this is the very first chunk, but second is not yet in the results, merging all in");
              }
              res.addAll(chunkRes);
            }
            else if (threadsNum - 1 == idx) {
              completedChunks[idx] = COMPLETED_FLAG;

              if (completedChunks[idx - 1] == COMPLETED_FLAG) {

                consoleLogger.debug("this is the very last chunk, merging the head only");

                int chunkHeadIndex = Collections.min(chunkRes.keySet());
                String headStr = String.valueOf(chunkRes.get(chunkHeadIndex));

                // find the RES last index
                int resultTailIndex = res.getMaxIndex();
                SaxRecord resTail = res.getByIndex(resultTailIndex);
                String resStr = String.valueOf(resTail.getPayload());

                consoleLogger.debug("last index in the res " + resultTailIndex + " for " + resStr
                    + ", first index in the tail " + chunkHeadIndex + " for " + headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && resStr.equalsIgnoreCase(headStr)) {
                  consoleLogger.debug("chunk head " + headStr + " at " + chunkHeadIndex
                      + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
                else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                    && (sp.checkMinDistIsZero(headStr.toCharArray(), resTail.getPayload()))) {
                  consoleLogger.debug("chunk head " + headStr + " at " + chunkHeadIndex
                      + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
              }
              else {
                consoleLogger
                    .debug("this is the very last chunk, but previous is not yet in the results, merging all in");
              }
              res.addAll(chunkRes);
            }
            else {
              // the other chunks
              //
              completedChunks[idx] = COMPLETED_FLAG;

              consoleLogger.debug("processing chunk " + idx);

              if (completedChunks[idx - 1] == COMPLETED_FLAG) {

                consoleLogger.debug("previous chunk was completed, merging in");

                int chunkHeadIndex = Collections.min(chunkRes.keySet());
                String headStr = String.valueOf(chunkRes.get(chunkHeadIndex));

                // find the RES last index
                int tmpIdx = chunkHeadIndex;
                while (null == res.getByIndex(tmpIdx)) {
                  tmpIdx--;
                }
                int resultTailIndex = tmpIdx;
                SaxRecord resTail = res.getByIndex(resultTailIndex);
                String resStr = String.valueOf(resTail.getPayload());

                consoleLogger.debug("last index in the res " + resultTailIndex + " for " + resStr
                    + ", first index in the chunk " + chunkHeadIndex + " for " + headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && resStr.equalsIgnoreCase(headStr)) {
                  consoleLogger.debug("chunk head " + headStr + " at " + chunkHeadIndex
                      + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
                else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                    && (sp.checkMinDistIsZero(headStr.toCharArray(), resTail.getPayload()))) {
                  consoleLogger.debug("chunk head " + headStr + " at " + chunkHeadIndex
                      + " is dropped in favor of res tail " + resStr + " at " + resultTailIndex);
                  chunkRes.remove(chunkHeadIndex);
                }
              }

              if (completedChunks[idx + 1] == COMPLETED_FLAG) {

                consoleLogger.debug("next chunk was completed, merging the tail");

                // chunk tail
                int chunkTailIdx = Collections.max(chunkRes.keySet());
                String tailStr = String.valueOf(chunkRes.get(chunkTailIdx));

                // res head
                int tmpIdx = chunkTailIdx;
                while (null == res.getByIndex(tmpIdx)) {
                  tmpIdx++;
                }
                int resultHeadIndex = tmpIdx;
                SaxRecord resultHead = res.getByIndex(resultHeadIndex);
                String headStr = String.valueOf(resultHead.getPayload());

                // print the log
                consoleLogger.debug("first index in the res " + resultHeadIndex + " for " + headStr
                    + ", last index in chunk " + chunkTailIdx + " for " + headStr);

                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && headStr.equalsIgnoreCase(tailStr)) {
                  consoleLogger.debug("res head " + headStr + " at " + resultHeadIndex
                      + " is dropped in favor of chunk tail " + tailStr + " at " + chunkTailIdx);
                  res.dropByIndex(resultHeadIndex);
                }
                else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                    && (sp.checkMinDistIsZero(tailStr.toCharArray(), resultHead.getPayload()))) {
                  consoleLogger.debug("res head " + headStr + " at " + resultHeadIndex
                      + " is dropped in favor of chunk tail " + tailStr + " at " + chunkTailIdx);
                  res.dropByIndex(resultHeadIndex);

                }
              }

              res.addAll(chunkRes);

            }
          }
        }
        totalTaskCounter--;
      }
    }
    catch (Exception e) {
      System.err.println("Error while waiting results: " + StackTrace.toString(e));
    }
    finally {
      // wait at least 1 more hour before terminate and fail
      try {
        if (!executorService.awaitTermination(4, TimeUnit.HOURS)) {
          executorService.shutdownNow(); // Cancel currently executing tasks
          if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
            System.err.println("Pool did not terminate... FATAL ERROR");
          }
        }
      }
      catch (InterruptedException ie) {
        System.err.println("Error while waiting interrupting: " + StackTrace.toString(ie));
        // (Re-)Cancel if current thread also interrupted
        executorService.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }

    }

    return res;
  }
}
