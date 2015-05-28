package net.seninp.jmotif.sax.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;

/**
 * Converts a single timeseries into a vector of values that represent occurrence frequencies of
 * n-grams.
 * 
 * @author psenin
 * 
 */
public class TSBitmapPrinter {

  // formatting parameters
  //
  // private static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
  // private static DecimalFormat df = new DecimalFormat("0.000000", otherSymbols);
  // and some constants
  private static final String QUOTE = "\'";
  private static final String COMMA = ",";
  private static final String CR = "\n";

  // classes needed for the workflow
  //
  private static final TSProcessor tsp = new TSProcessor();
  private static final SAXProcessor sp = new SAXProcessor();

  // private static final NormalAlphabet na = new NormalAlphabet();

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TSBitmapPrinter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws SAXException, IOException {

    BitmapParameters params = new BitmapParameters();
    JCommander jct = new JCommander(params, args);

    if (0 == args.length) {
      jct.usage();
    }
    else {
      // get params printed
      //
      StringBuffer sb = new StringBuffer(1024);
      sb.append("SAXBitmap CLI converter v.1").append(CR);
      sb.append("parameters:").append(CR);

      sb.append("  input file:                  ").append(BitmapParameters.IN_FILE).append(CR);
      sb.append("  output file:                 ").append(BitmapParameters.OUT_FILE).append(CR);
      sb.append("  SAX sliding window size:     ").append(BitmapParameters.SAX_WINDOW_SIZE).append(CR);
      sb.append("  SAX PAA size:                ").append(BitmapParameters.SAX_PAA_SIZE).append(CR);
      sb.append("  SAX alphabet size:           ").append(BitmapParameters.SAX_ALPHABET_SIZE).append(CR);
      sb.append("  SAX numerosity reduction:    ").append(BitmapParameters.SAX_NR_STRATEGY).append(CR);
      sb.append("  SAX normalization threshold: ").append(BitmapParameters.SAX_NORM_THRESHOLD).append(CR);

      sb.append("  Bitmap shingle size:         ").append(BitmapParameters.SHINGLE_SIZE).append(CR);
      
      sb.append(CR);
      System.out.println(sb.toString());

      // read the file
      //
      double[] data = tsp.readTS(BitmapParameters.IN_FILE, 0);
      consoleLogger.info("read " + data.length + " points from " + BitmapParameters.IN_FILE);

      Map<String, Integer> shingledData = sp.ts2Shingles(data, 
          BitmapParameters.SAX_WINDOW_SIZE, BitmapParameters.SAX_PAA_SIZE, BitmapParameters.SAX_ALPHABET_SIZE,
          BitmapParameters.SAX_NR_STRATEGY, BitmapParameters.SAX_NORM_THRESHOLD,
          BitmapParameters.SHINGLE_SIZE);

      StringBuffer shingles = new StringBuffer(BitmapParameters.SHINGLE_SIZE*(shingledData.size()+2));
      StringBuffer freqs = new StringBuffer(BitmapParameters.SHINGLE_SIZE*(shingledData.size()+2));
      TreeSet<String> keys = new TreeSet<String>(shingledData.keySet());
      for(String shingle : keys){
        shingles.append(QUOTE).append(shingle).append(QUOTE).append(COMMA);
        freqs.append(shingledData.get(shingle)).append(COMMA);
      }
      
      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(BitmapParameters.OUT_FILE)));
      bw.write(shingles.delete(shingles.length()-1, shingles.length()).toString());
      bw.write(CR);
      bw.write(freqs.delete(freqs.length()-1, freqs.length()).toString());
      bw.write(CR);
      bw.close();
      
    }

  }

}
