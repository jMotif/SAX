package net.seninp.jmotif.sax.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.UCRUtils;
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
public class UCRdataBitmapPrinter {

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
    consoleLogger = (Logger) LoggerFactory.getLogger(UCRdataBitmapPrinter.class);
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
      Map<String, List<double[]>> data = UCRUtils.readUCRData(BitmapParameters.IN_FILE);
      
      consoleLogger.info("read from " + BitmapParameters.IN_FILE);
      consoleLogger.info(UCRUtils.datasetStats(data, ""));

      Map<String, List<Integer[]>> res = new HashMap<String, List<Integer[]>>();
      TreeSet<String> keys = null;
      
      for(Entry<String, List<double[]>> e : data.entrySet()){
        String classLabel = e.getKey();
        for(double[] series : e.getValue()){
          
          Map<String, Integer> shingledData = sp.ts2Shingles(series, 
              BitmapParameters.SAX_WINDOW_SIZE, BitmapParameters.SAX_PAA_SIZE, BitmapParameters.SAX_ALPHABET_SIZE,
              BitmapParameters.SAX_NR_STRATEGY, BitmapParameters.SAX_NORM_THRESHOLD,
              BitmapParameters.SHINGLE_SIZE);
          
          if(!(res.containsKey(classLabel))){
            res.put(classLabel, new ArrayList<Integer[]>());
          }
          
          if(null == keys){
            keys = new TreeSet<String>(shingledData.keySet());
          }
          
          Integer[] arr = new Integer[keys.size()];
          int i=0;
          for(String shingle : keys){
            arr[i] = shingledData.get(shingle);
            i++;
          }
          
          res.get(classLabel).add(arr);
          
        }
        
      }
      
      consoleLogger.info("writing output...");
      
      
      StringBuffer shingles = new StringBuffer(BitmapParameters.SHINGLE_SIZE * (keys.size() + 2));
      for (String shingle : keys) {
        shingles.append(QUOTE).append(shingle).append(QUOTE).append(COMMA);
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(BitmapParameters.OUT_FILE)));
      bw.write("\'class_label\',"
          + shingles.delete(shingles.length() - 1, shingles.length()).toString());
      bw.write(CR);
      for (Entry<String, List<Integer[]>> e : res.entrySet()) {
        String classLabel = e.getKey();
        for (Integer[] arr : e.getValue()) {
          String str = Arrays.toString(arr).replaceAll("[\\[\\]\\s]", "");
          bw.write("\'" + classLabel + "\'" + COMMA + str + CR);
        }
      }
      bw.close();

      consoleLogger.info("done!");

    }

  }

}
