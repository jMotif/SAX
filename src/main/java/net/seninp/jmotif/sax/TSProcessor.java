package net.seninp.jmotif.sax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.sax.alphabet.Alphabet;

/**
 * Implements algorithms for low-level data manipulation.
 * 
 * @author Pavel Senin
 * 
 */
public class TSProcessor {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  /** The latin alphabet, lower case letters a-z. */
  public static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TSProcessor.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   */
  public TSProcessor() {
    super();
  }

  /**
   * Reads timeseries from a file. Assumes that file has a single double value on every line.
   * Assigned timestamps are the line numbers.
   * 
   * @param filename The file to read from.
   * @param columnIdx The column index.
   * @param sizeLimit The number of lines to read, 0 == all.
   * @return data.
   * @throws IOException if error occurs.
   * @throws SAXException if error occurs.
   */
  public static double[] readFileColumn(String filename, int columnIdx, int sizeLimit)
      throws IOException, SAXException {

    // make sure the path exists
    Path path = Paths.get(filename);
    if (!(Files.exists(path))) {
      throw new SAXException("unable to load data - data source not found.");
    }

    BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(filename), "UTF-8"));

    return readTS(br, columnIdx, sizeLimit);
  }

  /**
   * Reads timeseries from a file. Assumes that file has a single double value on every line.
   * Assigned timestamps are the line numbers.
   * 
   * @param br The reader to use.
   * @param columnIdx The column index.
   * @param sizeLimit The number of lines to read, 0 == all.
   * @return data.
   * @throws IOException if error occurs.
   * @throws SAXException if error occurs.
   */
  public static double[] readTS(BufferedReader br, int columnIdx, int sizeLimit)
      throws IOException, SAXException {
    ArrayList<Double> preRes = new ArrayList<Double>();
    int lineCounter = 0;

    String line = null;
    while ((line = br.readLine()) != null) {
      String[] split = line.trim().split("\\s+");
      if (split.length < columnIdx) {
        String message = "Unable to read data from column " + columnIdx;
        br.close();
        throw new SAXException(message);
      }
      String str = split[columnIdx];
      double num = Double.NaN;
      try {
        num = Double.valueOf(str);
      }
      catch (NumberFormatException e) {
        // consoleLogger.info("Skipping the row " + lineCounter + " with value \"" + str + "\"");
        continue;
      }
      preRes.add(num);
      lineCounter++;
      if ((0 != sizeLimit) && (lineCounter >= sizeLimit)) {
        break;
      }
    }
    br.close();
    double[] res = new double[preRes.size()];
    for (int i = 0; i < preRes.size(); i++) {
      res[i] = preRes.get(i);
    }
    return res;

  }

  /**
   * Read at least N elements from the one-column file.
   * 
   * @param dataFileName the file name.
   * @param loadLimit the load limit.
   * @return the read data or empty array if nothing to load.
   * @throws SAXException if error occurs.
   * @throws IOException if error occurs.
   */
  public double[] readTS(String dataFileName, int loadLimit) throws SAXException, IOException {

    Path path = Paths.get(dataFileName);
    if (!(Files.exists(path))) {
      throw new SAXException("unable to load data - data source not found.");
    }

    BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

    return readTS(reader, 0, loadLimit);

  }

  /**
   * Finds the maximal value in timeseries.
   * 
   * @param series The timeseries.
   * @return The max value.
   */
  public double max(double[] series) {
    double max = Double.MIN_VALUE;
    for (int i = 0; i < series.length; i++) {
      if (max < series[i]) {
        max = series[i];
      }
    }
    return max;
  }

  /**
   * Finds the minimal value in timeseries.
   * 
   * @param series The timeseries.
   * @return The min value.
   */
  public double min(double[] series) {
    double min = Double.MAX_VALUE;
    for (int i = 0; i < series.length; i++) {
      if (min > series[i]) {
        min = series[i];
      }
    }
    return min;
  }

  /**
   * Computes the mean value of timeseries.
   * 
   * @param series The timeseries.
   * @return The mean value.
   */
  public double mean(double[] series) {
    double res = 0D;
    int count = 0;
    for (double tp : series) {
      res += tp;
      count += 1;

    }
    if (count > 0) {
      return res / ((Integer) count).doubleValue();
    }
    return Double.NaN;
  }

  /**
   * Computes the median value of timeseries.
   * 
   * @param series The timeseries.
   * @return The median value.
   */
  public double median(double[] series) {
    double[] clonedSeries = series.clone();
    Arrays.sort(clonedSeries);

    double median;
    if (clonedSeries.length % 2 == 0) {
      median = (clonedSeries[clonedSeries.length / 2]
          + (double) clonedSeries[clonedSeries.length / 2 - 1]) / 2;
    }
    else {
      median = clonedSeries[clonedSeries.length / 2];
    }
    return median;
  }

  /**
   * Compute the variance of timeseries.
   * 
   * @param series The timeseries.
   * @return The variance.
   */
  public double var(double[] series) {
    double res = 0D;
    double mean = mean(series);
    int count = 0;
    for (double tp : series) {
      res += (tp - mean) * (tp - mean);
      count += 1;
    }
    if (count > 0) {
      return res / ((Integer) (count - 1)).doubleValue();
    }
    return Double.NaN;
  }

  /**
   * Speed-optimized implementation.
   * 
   * @param series The timeseries.
   * @return the standard deviation.
   */
  public double stDev(double[] series) {
    double num0 = 0D;
    double sum = 0D;
    int count = 0;
    for (double tp : series) {
      num0 = num0 + tp * tp;
      sum = sum + tp;
      count += 1;
    }
    double len = ((Integer) count).doubleValue();
    return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
  }

  /**
   * Speed-optimized Z-Normalize routine, doesn't care about normalization threshold.
   * 
   * @param series The timeseries.
   * @param normalizationThreshold the zNormalization threshold value.
   * @return Z-normalized time-series.
   */
  public double[] znorm(double[] series, double normalizationThreshold) {
    double[] res = new double[series.length];
    double mean = mean(series);
    double sd = stDev(series);
    if (sd < normalizationThreshold) {
      return series.clone();
    }
    for (int i = 0; i < res.length; i++) {
      res[i] = (series[i] - mean) / sd;
    }
    return res;
  }

  /**
   * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as
   * follows: 1) if all values of the piece are NaNs - the piece is approximated as NaN, 2) if there
   * are some (more or equal one) values happened to be in the piece - algorithm will handle it as
   * usual - getting the mean.
   * 
   * @param ts The timeseries to approximate.
   * @param paaSize The desired length of approximated timeseries.
   * @return PAA-approximated timeseries.
   */
  public double[] paa(double[] ts, int paaSize) {
    // fix the length
    int len = ts.length;
    // check for the trivial case
    if (len == paaSize) {
      return Arrays.copyOf(ts, ts.length);
    }
    else {
      if (len % paaSize == 0) {
        return colMeans(reshape(asMatrix(ts), len / paaSize, paaSize));
      }
      else {
        double[] paa = new double[paaSize];
        for (int i = 0; i < len * paaSize; i++) {
          int idx = i / len; // the spot
          int pos = i / paaSize; // the col spot
          paa[idx] = paa[idx] + ts[pos];
        }
        for (int i = 0; i < paaSize; i++) {
          paa[i] = paa[i] / (double) len;
        }
        return paa;
      }
    }

  }

  /**
   * Converts the timeseries into string using given cuts intervals. Useful for not-normal
   * distribution cuts.
   * 
   * @param vals The timeseries.
   * @param cuts The cut intervals.
   * @return The timeseries SAX representation.
   */
  public char[] ts2String(double[] vals, double[] cuts) {
    char[] res = new char[vals.length];
    for (int i = 0; i < vals.length; i++) {
      res[i] = num2char(vals[i], cuts);
    }
    return res;
  }

  /**
   * Convert the timeseries into the index using SAX cuts.
   * 
   * @param series The timeseries to convert.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size in use.
   * @return SAX representation of timeseries.
   * @throws Exception if error occurs.
   */
  public int[] ts2Index(double[] series, Alphabet alphabet, int alphabetSize) throws Exception {
    double[] cuts = alphabet.getCuts(alphabetSize);
    int[] res = new int[series.length];
    for (int i = 0; i < series.length; i++) {
      res[i] = num2index(series[i], cuts);
    }
    return res;
  }

  /**
   * Get mapping of a number to char.
   * 
   * @param value the value to map.
   * @param cuts the array of intervals.
   * @return character corresponding to numeric value.
   */
  public char num2char(double value, double[] cuts) {
    int count = 0;
    while ((count < cuts.length) && (cuts[count] <= value)) {
      count++;
    }
    return ALPHABET[count];
  }

  /**
   * Converts index into char.
   * 
   * @param idx The index value.
   * @return The char by index.
   */
  public char num2char(int idx) {
    return ALPHABET[idx];
  }

  /**
   * Get mapping of number to cut index.
   * 
   * @param value the value to map.
   * @param cuts the array of intervals.
   * @return character corresponding to numeric value.
   */
  public int num2index(double value, double[] cuts) {
    int count = 0;
    while ((count < cuts.length) && (cuts[count] <= value)) {
      count++;
    }
    return count;
  }

  /**
   * Extract subseries out of series.
   * 
   * @param series The series array.
   * @param start the fragment start.
   * @param end the fragment end.
   * @return The subseries.
   * @throws IndexOutOfBoundsException If error occurs.
   */
  public double[] subseriesByCopy(double[] series, int start, int end)
      throws IndexOutOfBoundsException {
    if ((start > end) || (start < 0) || (end > series.length)) {
      throw new IndexOutOfBoundsException("Unable to extract subseries, series length: "
          + series.length + ", start: " + start + ", end: " + String.valueOf(end - start));
    }
    return Arrays.copyOfRange(series, start, end);
  }

  /**
   * Prettyfies the timeseries for screen output.
   * 
   * @param series the data.
   * @param df the number format to use.
   * 
   * @return The timeseries formatted for screen output.
   */
  public String seriesToString(double[] series, NumberFormat df) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    for (double d : series) {
      sb.append(df.format(d)).append(',');
    }
    sb.delete(sb.length() - 2, sb.length() - 1).append("]");
    return sb.toString();
  }

  /**
   * Returns the m-by-n matrix B whose elements are taken column-wise from A. An error results if A
   * does not have m*n elements.
   * 
   * @param a the source matrix.
   * @param n number of rows in the new matrix.
   * @param m number of columns in the new matrix.
   * 
   * @return reshaped matrix.
   */
  public double[][] reshape(double[][] a, int n, int m) {
    int currentElement = 0;
    int aRows = a.length;

    double[][] res = new double[n][m];

    for (int j = 0; j < m; j++) {
      for (int i = 0; i < n; i++) {
        res[i][j] = a[currentElement % aRows][currentElement / aRows];
        currentElement++;
      }
    }
    return res;
  }

  /**
   * Converts the vector into one-row matrix.
   * 
   * @param vector The vector.
   * @return The matrix.
   */
  public double[][] asMatrix(double[] vector) {
    double[][] res = new double[1][vector.length];
    for (int i = 0; i < vector.length; i++) {
      res[0][i] = vector[i];
    }
    return res;
  }

  /**
   * Computes column means for the matrix.
   * 
   * @param a the input matrix.
   * @return result.
   */
  public double[] colMeans(double[][] a) {
    double[] res = new double[a[0].length];
    for (int j = 0; j < a[0].length; j++) {
      double sum = 0;
      int counter = 0;
      for (int i = 0; i < a.length; i++) {
        if (Double.isNaN(a[i][j]) || Double.isInfinite(a[i][j])) {
          continue;
        }
        sum += a[i][j];
        counter += 1;
      }
      if (counter == 0) {
        res[j] = Double.NaN;
      }
      else {
        res[j] = sum / ((Integer) counter).doubleValue();
      }
    }
    return res;
  }

  /**
   * Normalizes data in interval 0-1.
   * 
   * @param data the dataset.
   * @return normalized dataset.
   */
  public double[] normOne(double[] data) {
    double[] res = new double[data.length];
    double max = max(data);
    for (int i = 0; i < data.length; i++) {
      res[i] = data[i] / max;
    }
    return res;
  }

}
