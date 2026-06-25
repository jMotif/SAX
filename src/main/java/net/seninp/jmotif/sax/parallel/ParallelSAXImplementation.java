package net.seninp.jmotif.sax.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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

  // logging stuff
  //
  private static final Logger LOGGER;
  private static final Level LOGGING_LEVEL = Level.INFO;

  static {
    LOGGER = (Logger) LoggerFactory.getLogger(ParallelSAXImplementation.class);
    LOGGER.setLevel(LOGGING_LEVEL);
  }

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

    //
    // Numerosity reduction (EXACT and MINDIST) is order-dependent and therefore cannot be applied
    // safely inside the parallel workers, whose results merge in nondeterministic completion order.
    // Each worker always runs with NONE so the full, contiguous window-start sequence is
    // reconstructed regardless of merge order; the requested reduction is then applied as a single
    // deterministic post-pass over the merged, index-sorted result (identical to the sequential
    // implementation in SAXProcessor.ts2saxViaWindow).
    //
    NumerosityReductionStrategy nrStrategy = NumerosityReductionStrategy.NONE;

    ExecutorCompletionService<HashMap<Integer, char[]>> completionService = new ExecutorCompletionService<HashMap<Integer, char[]>>(
        executorService);

    int totalTaskCounter = 0;

    // this value used as a job id in future
    //
    final long tstamp = System.currentTimeMillis();

    // first chunk takes on the uneven division
    //
    int evenIncrement = timeseries.length / threadsNum;
    // The chunking/merge logic below assumes at least two chunks (a distinct first and last
    // chunk). With a single thread there is exactly one chunk worth of work, so the "last chunk"
    // would be submitted as a second task writing completedChunks[1] on a size-1 array, and the
    // first chunk would over-read the series past its end. Route the single-threaded case (and
    // any chunk that is too small to yield a window start) to the sequential implementation.
    if (threadsNum <= 1 || evenIncrement <= slidingWindowSize) {
      LOGGER.warn("Unable to run with {} threads. Rolling back to single-threaded implementation.",
          threadsNum);
      return sp.ts2saxViaWindow(timeseries, slidingWindowSize, paaSize, na.getCuts(alphabetSize),
          numRedStrategy, normalizationThreshold);
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

          LOGGER.debug("job with stamp {} has finished", chunkRes.get(-1));

          // drop the job-id marker entry
          chunkRes.remove(-1);

          // Workers run with NONE, so chunk results never overlap in window-start index and merge
          // order is irrelevant -- just collect every chunk's windows. The requested numerosity
          // reduction is applied once, deterministically, after all chunks are merged.
          //
          res.addAll(chunkRes);
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

    // Apply the requested numerosity reduction as a single deterministic post-pass over the
    // merged, index-sorted result. This walks the full NONE sequence in exactly the same order as
    // the sequential SAXProcessor.ts2saxViaWindow loop, so EXACT/MINDIST produce identical output.
    //
    if (NumerosityReductionStrategy.EXACT.equals(numRedStrategy)
        || NumerosityReductionStrategy.MINDIST.equals(numRedStrategy)) {

      SAXRecords newRes = new SAXRecords();
      ArrayList<Integer> keys = res.getAllIndices();
      char[] previousStr = null;
      for (int i : keys) {

        SAXRecord entry = res.getByIndex(i);

        if (null != previousStr) {
          if (NumerosityReductionStrategy.EXACT.equals(numRedStrategy)
              && Arrays.equals(entry.getPayload(), previousStr)) {
            continue;
          }
          else if (NumerosityReductionStrategy.MINDIST.equals(numRedStrategy)
              && sp.checkMinDistIsZero(entry.getPayload(), previousStr)) {
            continue;
          }
        }

        newRes.add(entry.getPayload(), i);
        previousStr = entry.getPayload();
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
