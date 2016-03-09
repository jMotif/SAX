package net.seninp.jmotif.sax.bitmap;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.util.HeatChart;

/**
 * Converts a single timeseries into a vector of values that represent occurrence frequencies of
 * n-grams.
 *
 * @author psenin
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
  private static final Logger LOGGER = LoggerFactory.getLogger(TSBitmapPrinter.class);

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
      sb.append("SAXBitmap CLI converter v.0.1").append(CR);
      sb.append("parameters:").append(CR);

      sb.append("  input file:                  ").append(BitmapParameters.IN_FILE).append(CR);
      sb.append("  output file:                 ").append(BitmapParameters.OUT_FILE).append(CR);
      sb.append("  SAX sliding window size:     ").append(BitmapParameters.SAX_WINDOW_SIZE)
          .append(CR);
      sb.append("  SAX PAA size:                ").append(BitmapParameters.SAX_PAA_SIZE).append(CR);
      sb.append("  SAX alphabet size:           ").append(BitmapParameters.SAX_ALPHABET_SIZE)
          .append(CR);
      sb.append("  SAX numerosity reduction:    ").append(BitmapParameters.SAX_NR_STRATEGY)
          .append(CR);
      sb.append("  SAX normalization threshold: ").append(BitmapParameters.SAX_NORM_THRESHOLD)
          .append(CR);

      sb.append("  Bitmap shingle size:         ").append(BitmapParameters.SHINGLE_SIZE).append(CR);

      if (null == BitmapParameters.BITMAP_FILE) {
        sb.append("  No bitmap will be produced").append(BitmapParameters.SHINGLE_SIZE).append(CR);
      }
      else {
        sb.append("  Bitmap filename specified: ").append(BitmapParameters.BITMAP_FILE).append(CR);
      }

      sb.append(CR);
      LOGGER.info("{}", sb.toString());

      // read the file
      //
      double[] data = tsp.readTS(BitmapParameters.IN_FILE, 0);
      LOGGER.info("read {} points from {}", +data.length, BitmapParameters.IN_FILE);

      Map<String, Integer> shingledData = sp.ts2Shingles(data, BitmapParameters.SAX_WINDOW_SIZE,
          BitmapParameters.SAX_PAA_SIZE, BitmapParameters.SAX_ALPHABET_SIZE,
          BitmapParameters.SAX_NR_STRATEGY, BitmapParameters.SAX_NORM_THRESHOLD,
          BitmapParameters.SHINGLE_SIZE);

      LOGGER.info("writing output...");

      StringBuilder shingles = new StringBuilder(
          BitmapParameters.SHINGLE_SIZE * (shingledData.size() + 2));
      StringBuilder freqs = new StringBuilder(
          BitmapParameters.SHINGLE_SIZE * (shingledData.size() + 2));
      TreeSet<String> keys = new TreeSet<String>(shingledData.keySet());
      for (String shingle : keys) {
        shingles.append(QUOTE).append(shingle).append(QUOTE).append(COMMA);
        freqs.append(shingledData.get(shingle)).append(COMMA);
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(BitmapParameters.OUT_FILE)));
      bw.write(shingles.delete(shingles.length() - 1, shingles.length()).toString());
      bw.write(CR);
      bw.write(freqs.delete(freqs.length() - 1, freqs.length()).toString());
      bw.write(CR);
      bw.close();

      if (null == BitmapParameters.BITMAP_FILE) {
        System.exit(10);
      }

      if (16 == shingledData.size()) {
        double[][] heatmapData = new double[4][4];
        int counter = 0;
        for (String shingle : keys) {
          Integer value = shingledData.get(shingle);
          heatmapData[counter / 4][counter % 4] = value;
          counter++;
        }
        HeatChart chart = new HeatChart(heatmapData);
        chart.setAxisThickness(0);
        chart.setTitle(BitmapParameters.IN_FILE);
        chart.setCellSize(new Dimension(64, 64));
        chart.saveToFile(new File("my-chart.png"));
      }
      else if (64 == shingledData.size()) {
        double[][] heatmapData = new double[8][8];
        int counter = 0;
        for (String shingle : keys) {
          Integer value = shingledData.get(shingle);
          heatmapData[counter / 8][counter % 8] = value;
          counter++;
        }
        HeatChart chart = new HeatChart(heatmapData);
        chart.setAxisThickness(0);
        chart.setTitle(BitmapParameters.IN_FILE);
        chart.setCellSize(new Dimension(32, 32));
        chart.saveToFile(new File(BitmapParameters.BITMAP_FILE));
      }
      else {
        LOGGER.error("Bitmap is produced for 16 or 64 shingles only.");
      }

      LOGGER.info("done!");

    }

  }

}
