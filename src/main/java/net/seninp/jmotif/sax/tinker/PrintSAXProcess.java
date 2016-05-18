package net.seninp.jmotif.sax.tinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class PrintSAXProcess {

  private static final String INPUT_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int SAX_WIN_SIZE = 160;
  private static final int SAX_PAA_SIZE = 4;
  private static final int SAX_A_SIZE = 4;
  private static final double SAX_NORM_THRESHOLD = 0.001;

  private static final TSProcessor tsProcessor = new TSProcessor();
  private static final Alphabet na = new NormalAlphabet();
  private static final SAXProcessor sp = new SAXProcessor();

  private static final String TAB = "\t";

  private static final Object CR = "\n";

  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.EXACT;

  public static void main(String[] args) throws IOException, SAXException {

    double[] ts = TSProcessor.readFileColumn(INPUT_FNAME, 0, 0);

    double[] cuts = na.getCuts(SAX_A_SIZE);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("test_sax.txt")));

    // scan across the time series extract sub sequences, and convert them to strings
    char[] previousString = null;

    for (int i = 0; i <= ts.length - SAX_WIN_SIZE; i++) {

      StringBuffer sb = new StringBuffer();
      sb.append(i).append(TAB);

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(ts, i, i + SAX_WIN_SIZE);
      sb.append(Arrays.toString(subSection).replaceAll("\\s+", "")).append(TAB);

      // Z normalize it
      subSection = tsProcessor.znorm(subSection, SAX_NORM_THRESHOLD);

      // perform PAA conversion if needed
      double[] paa = tsProcessor.paa(subSection, SAX_PAA_SIZE);
      sb.append(Arrays.toString(paa).replaceAll("\\s+", "")).append(TAB);

      // Convert the PAA to a string.
      char[] currentString = tsProcessor.ts2String(paa, cuts);
      sb.append("\"").append(currentString).append("\"").append(TAB);

      if (null != previousString) {

        if (NumerosityReductionStrategy.EXACT.equals(NR_STRATEGY)
            && Arrays.equals(previousString, currentString)) {
          // NumerosityReduction
          sb.append("skipped").append(CR);
          bw.write(sb.toString());
          continue;
        }
        else if (NumerosityReductionStrategy.MINDIST.equals(NR_STRATEGY)
            && sp.checkMinDistIsZero(previousString, currentString)) {
          continue;
        }

      }

      previousString = currentString;
      sb.append("kept").append(CR);
      bw.write(sb.toString());

    }

    bw.close();

  }

}
