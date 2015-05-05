package net.seninp.jmotif.sax;

import java.util.Arrays;
import java.util.Set;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This implements a simple CLI tool for ad-hoc SAX discretization.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXcliConverter {

  private static final String CR = "\n";
  private static final String COMMA = ", ";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXcliConverter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   */
  private SAXcliConverter() {
    assert true;
  }

  /**
   * The main runnable.
   * 
   * @param args the command line parameters.
   */
  public static void main(String[] args) {

    try {

      if (args.length < 6 || args.length > 7) {
        System.out.println(printHelp());
        System.exit(-1);
      }

      String dataFName = args[0];
      double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

      Integer slidingWindowSize = Integer.valueOf(args[1]);
      Integer paaSize = Integer.valueOf(args[2]);
      Integer alphabetSize = Integer.valueOf(args[3]);

      NumerosityReductionStrategy nrStrategy = NumerosityReductionStrategy.valueOf(args[4]
          .toUpperCase());

      Double nThreshold = Double.valueOf(args[5]);

      NormalAlphabet na = new NormalAlphabet();
      SAXProcessor sp = new SAXProcessor();

      Integer threadsNum = 1;
      SAXRecords res = null;

      if (args.length > 6) {
        threadsNum = Integer.valueOf(args[6]);
      }

      if (threadsNum > 1) {
        ParallelSAXImplementation ps = new ParallelSAXImplementation();
        res = ps.process(ts, threadsNum, slidingWindowSize, paaSize, alphabetSize, nrStrategy,
            nThreshold);
      }
      else {
        res = sp.ts2saxViaWindow(ts, slidingWindowSize, paaSize, na.getCuts(alphabetSize),
            nrStrategy, nThreshold);
      }

      Set<Integer> index = res.getIndexes();
      for (Integer idx : index) {
        System.out.println(idx + COMMA + String.valueOf(res.getByIndex(idx).getPayload()));
      }

    }
    catch (Exception e) {
      System.err.println("error occured while parsing parameters " + Arrays.toString(args) + CR
          + StackTrace.toString(e));
      System.exit(-1);
    }

  }

  /**
   * Prints the CLI help.
   * 
   * @return formatted help string.
   */
  private static String printHelp() {
    StringBuffer sb = new StringBuffer(512);
    sb.append("Command-line SAX conversion utility, the output printed to STDOUT ")
        .append(CR)
        .append("Expects 6 parameters:")
        .append(CR)
        .append(" [1] training dataset filename")
        .append(CR)
        .append(" [2] sliding window size")
        .append(CR)
        .append(" [3] PAA size")
        .append(CR)
        .append(" [4] Alphabet size")
        .append(CR)
        .append(" [5] numerosity reduction <NONE|EXACT|MINDIST>")
        .append(CR)
        .append(" [6] z-Normalization threshold value")
        .append(CR)
        .append(" [7] OPTIONAL: number of threads to use")
        .append(CR)
        .append(
            "An execution example: $java -jar \"target/jmotif-sax-0.0.1-SNAPSHOT-jar-with-dependencies.jar\" ")
        .append("src/resources/test-data/ecg0606_1.csv 120 7 5 EXACT 0.001 2").append(CR);
    return sb.toString();
  }

}
