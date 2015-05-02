package net.seninp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This implements variety utils for UCR-formatted data.
 * 
 * @author psenin
 * 
 */
public class UCRUtils {

  /**
   * Reads bunch of series from file. First column treats as a class label. Rest as a real-valued
   * series.
   * 
   * @param fileName
   * @return
   * @throws IOException
   */
  public static Map<String, List<double[]>> readUCRData(String fileName) throws IOException {

    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();

    BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
    String line = "";
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) {
        continue;
      }
      String[] split = line.trim().split("[\\,\\s]+");

      String label = split[0];
      Double num = parseValue(label);
      String seriesType = label;
      if (!(Double.isNaN(num))) {
        seriesType = String.valueOf(num.intValue());
      }
      double[] series = new double[split.length - 1];
      for (int i = 1; i < split.length; i++) {
        series[i - 1] = Double.valueOf(split[i].trim()).doubleValue();
      }

      if (!res.containsKey(seriesType)) {
        res.put(seriesType, new ArrayList<double[]>());
      }

      res.get(seriesType).add(series);
    }

    br.close();
    return res;

  }

  public static String datasetStats(Map<String, List<double[]>> data, String prefix) {

    int globalMinLength = Integer.MAX_VALUE;
    int globalMaxLength = Integer.MIN_VALUE;

    double globalMinValue = Double.MAX_VALUE;
    double globalMaxValue = Double.MIN_VALUE;

    for (Entry<String, List<double[]>> e : data.entrySet()) {
      for (double[] dataEntry : e.getValue()) {

        globalMaxLength = (dataEntry.length > globalMaxLength) ? dataEntry.length : globalMaxLength;
        globalMinLength = (dataEntry.length < globalMinLength) ? dataEntry.length : globalMinLength;

        for (double value : dataEntry) {
          globalMaxValue = (value > globalMaxValue) ? value : globalMaxValue;
          globalMinValue = (value < globalMinValue) ? value : globalMinValue;
        }

      }
    }
    StringBuffer sb = new StringBuffer();

    sb.append(prefix).append(" classes: ").append(data.size());
    sb.append(", series length min: ").append(globalMinLength);
    sb.append(", max: ").append(globalMaxLength);
    sb.append(", min value: ").append(globalMinValue);
    sb.append(", max value: ").append(globalMaxValue).append(";");
    for (Entry<String, List<double[]>> e : data.entrySet()) {
      sb.append(prefix).append(" class: ").append(e.getKey());
      sb.append(" series: ").append(e.getValue().size()).append(";");
    }

    return sb.delete(sb.length() - 1, sb.length()).toString();
  }

  private static Double parseValue(String string) {
    Double res = Double.NaN;
    try {
      Double r = Double.valueOf(string);
      res = r;
    }
    catch (NumberFormatException e) {
      assert true;
    }
    return res;
  }

}
