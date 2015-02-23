package edu.hawaii.jmotif.timeseries;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Implements algorithms for low-level data manipulation.
 * 
 * @author Pavel Senin
 * 
 */
public final class TSProcessor {

  /** The latin alphabet, lower case letters a-z. */
  static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  public static final double GLOBAL_NORMALIZATION_THRESHOLD = 0.05D;

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
   * @throws NumberFormatException if error occurs.
   * @throws IOException if error occurs.
   * @throws TSException if error occurs.
   */
  public double[] readFileColumn(String filename, int columnIdx, int sizeLimit)
      throws NumberFormatException, IOException, TSException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
        "UTF-8"));

    ArrayList<Double> preRes = new ArrayList<Double>();
    int lineCounter = 0;

    String line = null;
    while ((line = br.readLine()) != null) {
      String[] split = line.split("\\s+");
      if (split.length < columnIdx) {
        String message = "Unable to read data from column " + columnIdx + " of file " + filename;
        br.close();
        throw new TSException(message);
      }
      preRes.add(Double.valueOf(line));
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
   * Computes the mean for integer series.
   * 
   * @param series
   * @return
   */
  public int mean(int[] series) {
    int res = 0;
    int count = 0;
    for (int tp : series) {
      res += tp;
      count += 1;
    }
    return res / count;
  }

  /**
   * Computes the autocorrelation value of timeseries. according to algorithm in:
   * http://www.itl.nist.gov/div898/handbook/eda/section3/eda35c.htm
   * 
   * @param series The timeseries.
   * @param lag The lag
   * @return The autocorrelation value.
   */
  public double autocorrelation(double series[], int lag) {
    double ac = 0;

    double avg = mean(series);
    double numerator = 0;
    for (int i = 0; i < series.length - lag; i++) {
      if (Double.isNaN(series[i]) || Double.isInfinite(series[i])) {
        continue;
      }
      numerator += (series[i] - avg) * (series[i + lag] - avg);
    }
    double denominator = 0;
    for (int i = 0; i < series.length; i++) {
      if (Double.isNaN(series[i]) || Double.isInfinite(series[i])) {
        continue;
      }
      denominator += (series[i] - avg) * (series[i] - avg);
    }
    ac = numerator / denominator;
    return ac;
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
   * @param normalizationThreshold
   * @return Z-normalized time-series.
   * @throws TSException if error occurs.
   */
  public double[] optimizedZNorm(double[] series, double normalizationThreshold) {
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
   * @throws TSException if error occurs.
   */
  public double[] paa(double[] ts, int paaSize) throws TSException {
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
   * Extract subseries out of series.
   * 
   * @param series The series array.
   * @param start Start position
   * @param length Length of subseries to extract.
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
   * Implements Gaussian smoothing.
   * 
   * @param series Data to process.
   * @param filterWidth The filter width.
   * @return smoothed series.
   * @throws TSException if error occurs.
   */
  public double[] gaussFilter(double[] series, double filterWidth) throws TSException {

    double[] smoothedSignal = new double[series.length];
    double sigma = filterWidth / 2D;
    int maxShift = (int) Math.floor(4D * sigma); // Gaussian curve is reasonably > 0

    if (maxShift < 1) {
      throw new TSException("NOT smoothing: filter width too small - " + filterWidth);
    }
    for (int i = 0; i < smoothedSignal.length; i++) {
      smoothedSignal[i] = series[i];

      if (maxShift < 1) {
        continue;
      }
      for (int j = 1; j <= maxShift; j++) {

        double gaussFilter = Math.exp(-(j * j) / (2. * sigma * sigma));
        double leftAmpl, rightAmpl;

        // go left
        if ((i - j) >= 0) {
          leftAmpl = series[i - j];
        }
        else {
          leftAmpl = series[i];
        }

        // go right
        if ((i + j) <= smoothedSignal.length - 1) {
          rightAmpl = series[i + j];
        }
        else {
          rightAmpl = series[i];
        }

        smoothedSignal[i] += gaussFilter * (leftAmpl + rightAmpl);

      }

      double normalizingCoef = Math.sqrt(2. * Math.PI) * sigma;
      smoothedSignal[i] /= normalizingCoef;

    }
    return smoothedSignal;
  }

  public double gaussian(double x, double filterWidth) {
    double sigma = filterWidth / 2.;
    return Math.exp(-(x * x) / (2. * sigma * sigma));
  }

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
}
