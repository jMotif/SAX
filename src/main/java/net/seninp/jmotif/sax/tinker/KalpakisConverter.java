package net.seninp.jmotif.sax.tinker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.bitmap.BitmapParameters;

public class KalpakisConverter {

  private static final String[] filenames = { "normal_16.txt", "normal_18.txt", "normal_2.txt",
      "normal_4.txt", "normal_6.txt", "normal_8.txt" };

  private static final String pathPrefix = "src/resources/bitmap/ECGData/normal/";

  private static final int WIN_SIZE = 50;

  private static final int PAA_SIZE = 10;

  private static final int A_SIZE = 4;

  private static final int LEVEL = 2;

  private static final String QUOTE = "'";
  private static final String COMMA = ",";
  private static final char CR = '\n';

  public static void main(String[] args) throws IOException, SAXException {

    SAXProcessor sp = new SAXProcessor();

    for (String fname : filenames) {
      String inFname = pathPrefix + fname;
      String outFname = inFname + ".shingled.txt";

      double[] series = TSProcessor.readFileColumn(inFname, 0, 0);
      System.err.println("read " + inFname + ", " + series.length + " points ...");

      Map<String, Integer> shingledData = sp.ts2Shingles(series, WIN_SIZE, PAA_SIZE, A_SIZE,
          NumerosityReductionStrategy.NONE, 0.001, LEVEL);

      StringBuilder shingles = new StringBuilder(
          BitmapParameters.SHINGLE_SIZE * (shingledData.size() + 2));
      StringBuilder freqs = new StringBuilder(
          BitmapParameters.SHINGLE_SIZE * (shingledData.size() + 2));
      TreeSet<String> keys = new TreeSet<String>(shingledData.keySet());
      for (String shingle : keys) {
        shingles.append(QUOTE).append(shingle).append(QUOTE).append(COMMA);
        freqs.append(shingledData.get(shingle)).append(COMMA);
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFname)));
      bw.write(shingles.delete(shingles.length() - 1, shingles.length()).toString());
      bw.write(CR);
      bw.write(freqs.delete(freqs.length() - 1, freqs.length()).toString());
      bw.write(CR);
      bw.close();

    }

  }

}
