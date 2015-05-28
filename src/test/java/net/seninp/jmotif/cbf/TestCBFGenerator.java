package net.seninp.jmotif.cbf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This code I used to asses generator correctness through R plotting.
 * 
 * @author psenin
 * 
 */
public class TestCBFGenerator {

  /** The timeseries length. */
  private static final int SERIES_LENGTH = 128;
  private static final int REPEATS = 5;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    // ticks
    int[] t = new int[SERIES_LENGTH];
    for (int i = 0; i < SERIES_LENGTH; i++) {
      t[i] = i;
    }

    // cylinder sample
    List<double[]> cylinders = new ArrayList<double[]>();
    for (int k = 0; k < 35; k++) {
      double[] arr = new double[SERIES_LENGTH * REPEATS];
      for (int i = 0; i < REPEATS; i++) {
        double[] c = CBFGenerator.cylinder(t);
        for (int j = 0; j < SERIES_LENGTH; j++) {
          arr[i * SERIES_LENGTH + j] = c[j];
        }
      }
      cylinders.add(arr);
    }
    save("RCode/CBF_explorer/cylinder.csv", "1", cylinders);

    // bell sample
    List<double[]> bells = new ArrayList<double[]>();
    for (int k = 0; k < 35; k++) {
      double[] arr = new double[SERIES_LENGTH * REPEATS];
      for (int i = 0; i < REPEATS; i++) {
        double[] c = CBFGenerator.bell(t);
        for (int j = 0; j < SERIES_LENGTH; j++) {
          arr[i * SERIES_LENGTH + j] = c[j];
        }
      }
      bells.add(arr);
    }
    save("RCode/CBF_explorer/bell.csv", "2", bells);

    // funnel sample
    List<double[]> funnels = new ArrayList<double[]>();
    for (int k = 0; k < 35; k++) {
      double[] arr = new double[SERIES_LENGTH * REPEATS];
      for (int i = 0; i < REPEATS; i++) {
        double[] c = CBFGenerator.funnel(t);
        for (int j = 0; j < SERIES_LENGTH; j++) {
          arr[i * SERIES_LENGTH + j] = c[j];
        }
      }
      funnels.add(arr);
    }
    save("RCode/CBF_explorer/funnel.csv", "3", funnels);

  }

  private static void save(String fname, String prefix, List<double[]> data) throws IOException {
    BufferedWriter bw = new BufferedWriter(new FileWriter(fname));
    for (double[] arr : data) {
      bw.write(prefix + " "
          + Arrays.toString(arr).replace("[", "").replace("]", "").replace(",", " ") + "\n");
    }
    bw.close();
  }
}
