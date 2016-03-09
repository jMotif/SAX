package net.seninp.jmotif.sax.bitmap;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.util.HeatChart;
import net.seninp.util.UCRUtils;

/**
 * Converts a single timeseries into a vector of values that represent occurrence frequencies of
 * n-grams.
 *
 * @author psenin
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
  private static final SAXProcessor sp = new SAXProcessor();

  // private static final NormalAlphabet na = new NormalAlphabet();

  // logging stuff
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(UCRdataBitmapPrinter.class);

  public static void main(String[] args) throws SAXException, IOException {

    BitmapParameters params = new BitmapParameters();
    JCommander jct = new JCommander(params, args);

    if (0 == args.length) {
      jct.usage();
    }
    else {
      // get params printed
      //
      StringBuilder sb = new StringBuilder(1024);
      sb.append("SAXBitmap CLI converter v.1").append(CR);
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
        sb.append("  Bitmap filename specified:   ").append(BitmapParameters.BITMAP_FILE)
            .append(CR);
      }

      sb.append(CR);
      LOGGER.info("{}", sb.toString());

      // read the file
      //
      Map<String, List<double[]>> data = UCRUtils.readUCRData(BitmapParameters.IN_FILE);

      LOGGER.info("read from {}", BitmapParameters.IN_FILE);
      LOGGER.info("{}", UCRUtils.datasetStats(data, ""));

      // resulting shingle frequencies and the keys array
      //
      Map<String, List<Integer[]>> res = new HashMap<String, List<Integer[]>>();
      TreeSet<String> shinglesSet = null;

      for (Entry<String, List<double[]>> e : data.entrySet()) {
        String classLabel = e.getKey();
        for (double[] series : e.getValue()) {

          Map<String, Integer> shingledData = sp.ts2Shingles(series,
              BitmapParameters.SAX_WINDOW_SIZE, BitmapParameters.SAX_PAA_SIZE,
              BitmapParameters.SAX_ALPHABET_SIZE, BitmapParameters.SAX_NR_STRATEGY,
              BitmapParameters.SAX_NORM_THRESHOLD, BitmapParameters.SHINGLE_SIZE);

          if (!(res.containsKey(classLabel))) {
            res.put(classLabel, new ArrayList<Integer[]>());
          }

          if (null == shinglesSet) {
            shinglesSet = new TreeSet<String>(shingledData.keySet());
          }

          Integer[] arr = new Integer[shinglesSet.size()];
          int i = 0;
          for (String shingle : shinglesSet) {
            arr[i] = shingledData.get(shingle);
            i++;
          }

          res.get(classLabel).add(arr);

        }

      }

      // produce the output
      //
      LOGGER.info("writing shingled output...");

      StringBuffer shinglesStr = new StringBuffer(
          BitmapParameters.SHINGLE_SIZE * (shinglesSet.size() + 2));
      for (String shingle : shinglesSet) {
        shinglesStr.append(QUOTE).append(shingle).append(QUOTE).append(COMMA);
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(BitmapParameters.OUT_FILE)));
      bw.write("\'class_label\',"
          + shinglesStr.delete(shinglesStr.length() - 1, shinglesStr.length()).toString());
      bw.write(CR);
      for (Entry<String, List<Integer[]>> e : res.entrySet()) {
        String classLabel = e.getKey();
        for (Integer[] arr : e.getValue()) {
          String str = Arrays.toString(arr).replaceAll("[\\[\\]\\s]", "");
          bw.write("\'" + classLabel + "\'" + COMMA + str + CR);
        }
      }
      bw.close();

      LOGGER.info("done!");

      // produce the bitmap
      //
      if (null == BitmapParameters.BITMAP_FILE) {
        System.exit(10);
      }

      LOGGER.info("producing bitmap for the dataset");

      // remove the columns which are all zeros, build an index of those
      //
      HashSet<Integer> zeroIndices = new HashSet<Integer>(shinglesSet.size());
      for (int i = 0; i < shinglesSet.size(); i++) {
        zeroIndices.add(i);
      }

      // count the number of rows needed and refine zeroed columns
      //
      int rows = 0;
      for (Entry<String, List<Integer[]>> e : res.entrySet()) {
        rows = rows + e.getValue().size();
        for (Integer[] arr : e.getValue()) {
          HashSet<Integer> tmpZeroes = new HashSet<Integer>();
          for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(0)) {
              tmpZeroes.add(i);
            }
          }
          zeroIndices.retainAll(tmpZeroes);
        }
      }

      ArrayList<String> prunedShingles = new ArrayList<String>();
      int counter = 0;
      for (String shingle : shinglesSet) {
        if (zeroIndices.contains(counter)) {
          prunedShingles.add(shingle);
        }
        counter++;
      }
      LOGGER.info("dropped zero-column shingles: {}",
          Arrays.toString(prunedShingles.toArray(new String[prunedShingles.size()])));

      // future heatmap datastructure
      //
      double[][] heatmapData = new double[rows][shinglesSet.size() - zeroIndices.size()];

      // make the Y labels data
      //
      ArrayList<String> yLabels = new ArrayList<String>();
      //
      // and fill the rows
      int currRow = 0;
      for (Entry<String, List<Integer[]>> e : res.entrySet()) {
        int currArrayIdx = 0;
        for (Integer[] arr : e.getValue()) {
          yLabels.add(e.getKey() + "_" + currArrayIdx);
          heatmapData[currRow] = toDoubleAray(arr, zeroIndices);
          currRow++;
          currArrayIdx++;
        }
      }

      // makeup a heatmap
      //
      HeatChart chart = new HeatChart(heatmapData);

      chart.setAxisColour(Color.WHITE);
      chart.setAxisThickness(2);

      chart.setYValues(yLabels.toArray(new String[yLabels.size()]));
      chart.setShowYAxisValues(true);

      chart.setXValues(
          toShingleLabelsArray(shinglesSet.toArray(new String[shinglesSet.size()]), zeroIndices));
      chart.setShowXAxisValues(true);
      chart.setXValuesHorizontal(false);

      chart.setTitle(BitmapParameters.IN_FILE);
      chart.setCellSize(new Dimension(10, 10));
      chart.saveToFile(new File(BitmapParameters.BITMAP_FILE));

    }

  }

  private static String[] toShingleLabelsArray(String[] array, HashSet<Integer> zeroIndices) {
    String[] res = new String[array.length - zeroIndices.size()];
    int skip = 0;
    for (int i = 0; i < array.length; i++) {
      if (zeroIndices.contains(i)) {
        skip++;
        continue;
      }
      res[i - skip] = array[i];
    }
    return res;
  }

  /**
   * Converts an array into array of doubles skipping specified indeces.
   *
   * @param intArray the input array.
   * @param skipIndex skip index list.
   * @return array of doubles.
   */
  private static double[] toDoubleAray(Integer[] intArray, HashSet<Integer> skipIndex) {
    double[] res = new double[intArray.length - skipIndex.size()];
    int skip = 0;
    for (int i = 0; i < intArray.length; i++) {
      if (skipIndex.contains(i)) {
        skip++;
        continue;
      }
      res[i - skip] = intArray[i].doubleValue();
    }
    return res;
  }
}
