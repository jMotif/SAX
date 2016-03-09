package net.seninp.jmotif.sax;

import com.beust.jcommander.JCommander;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This implements a simple CLI tool for ad-hoc SAX discretization.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXCLIConverter {

  private static final String CR = "\n";
  private static final String COMMA = ", ";

  /**
   * Constructor.
   */
  private SAXCLIConverter() {
    assert true;
  }

  /**
   * The main runnable.
   * 
   * @param args the command line parameters.
   */
  public static void main(String[] args) {

    try {

      SAXCLIParameters params = new SAXCLIParameters();
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

        sb.append("  input file:                  ").append(SAXCLIParameters.IN_FILE).append(CR);
        sb.append("  output file:                 ").append(SAXCLIParameters.OUT_FILE).append(CR);
        sb.append("  SAX sliding window size:     ").append(SAXCLIParameters.SAX_WINDOW_SIZE)
            .append(CR);
        sb.append("  SAX PAA size:                ").append(SAXCLIParameters.SAX_PAA_SIZE)
            .append(CR);
        sb.append("  SAX alphabet size:           ").append(SAXCLIParameters.SAX_ALPHABET_SIZE)
            .append(CR);
        sb.append("  SAX numerosity reduction:    ").append(SAXCLIParameters.SAX_NR_STRATEGY)
            .append(CR);
        sb.append("  SAX normalization threshold: ").append(SAXCLIParameters.SAX_NORM_THRESHOLD)
            .append(CR);
        sb.append("  threads to use:              ").append(SAXCLIParameters.THREADS_NUM)
            .append(CR);

        String dataFName = SAXCLIParameters.IN_FILE;
        double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

        Integer slidingWindowSize = Integer.valueOf(SAXCLIParameters.SAX_WINDOW_SIZE);
        Integer paaSize = Integer.valueOf(SAXCLIParameters.SAX_PAA_SIZE);
        Integer alphabetSize = Integer.valueOf(SAXCLIParameters.SAX_ALPHABET_SIZE);

        NumerosityReductionStrategy nrStrategy = SAXCLIParameters.SAX_NR_STRATEGY;

        Double nThreshold = SAXCLIParameters.SAX_NORM_THRESHOLD;

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

        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.addAll(res.getIndexes());
        Collections.sort(indexes);

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(SAXCLIParameters.OUT_FILE)));
        for (Integer idx : indexes) {
          bw.write(idx + COMMA + String.valueOf(res.getByIndex(idx).getPayload()) + CR);
        }
        bw.close();

      }
    }
    catch (IOException | SAXException e) {
      System.err.println("error occured while parsing parameters " + Arrays.toString(args) + CR
          + StackTrace.toString(e));
      System.exit(-1);
    }

  }

}
