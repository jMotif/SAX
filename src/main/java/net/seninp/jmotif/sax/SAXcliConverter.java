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
import com.beust.jcommander.JCommander;

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

      SAXCliParameters params = new SAXCliParameters();
      JCommander jct = new JCommander(params, args);

      if (0 == args.length) {
        jct.usage();
      }
      else {
        // get params printed
        //
        StringBuffer sb = new StringBuffer(1024);
        sb.append("SAX CLI converter v.1").append(CR);
        sb.append("parameters:").append(CR);

        sb.append("  input file:                  ").append(SAXCliParameters.IN_FILE).append(CR);
        sb.append("  output file:                 ").append(SAXCliParameters.OUT_FILE).append(CR);
        sb.append("  SAX sliding window size:     ").append(SAXCliParameters.SAX_WINDOW_SIZE)
            .append(CR);
        sb.append("  SAX PAA size:                ").append(SAXCliParameters.SAX_PAA_SIZE)
            .append(CR);
        sb.append("  SAX alphabet size:           ").append(SAXCliParameters.SAX_ALPHABET_SIZE)
            .append(CR);
        sb.append("  SAX numerosity reduction:    ").append(SAXCliParameters.SAX_NR_STRATEGY)
            .append(CR);
        sb.append("  SAX normalization threshold: ").append(SAXCliParameters.SAX_NORM_THRESHOLD)
            .append(CR);

        sb.append("  threads to use:         ").append(SAXCliParameters.THREADS_NUM).append(CR);

        String dataFName = SAXCliParameters.IN_FILE;
        double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

        Integer slidingWindowSize = Integer.valueOf(SAXCliParameters.SAX_WINDOW_SIZE);
        Integer paaSize = Integer.valueOf(SAXCliParameters.SAX_PAA_SIZE);
        Integer alphabetSize = Integer.valueOf(SAXCliParameters.SAX_ALPHABET_SIZE);

        NumerosityReductionStrategy nrStrategy = SAXCliParameters.SAX_NR_STRATEGY;

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
    }
    catch (Exception e) {
      System.err.println("error occured while parsing parameters " + Arrays.toString(args) + CR
          + StackTrace.toString(e));
      System.exit(-1);
    }

  }

}
