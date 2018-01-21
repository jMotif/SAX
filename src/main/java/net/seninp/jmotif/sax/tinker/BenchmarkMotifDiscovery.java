package net.seninp.jmotif.sax.tinker;

import java.io.Console;
import java.util.Date;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.motif.EMMAImplementation;
import net.seninp.jmotif.sax.motif.MotifRecord;

public class BenchmarkMotifDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/300_signal1.txt";

  private static final int MOTIF_SIZE = 300;
  private static final double MOTIF_RANGE = 5.;
  private static final int PAA_SIZE = 30;
  private static final int ALPHABET_SIZE = 6;

  private static final double ZNORM_THRESHOLD = 0.01;

  private static double[] series;

  private static TSProcessor tp = new TSProcessor();

  public static void main(String[] args) throws Exception {

    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);

    Console c = System.console();
    if (c != null) {
      c.format("\nPress ENTER to proceed.\n");
      c.readLine();
    }

    Date start = new Date();
    // motifsBF = BruteForceMotifImplementation.series2BruteForceMotifs(series, MOTIF_SIZE,
    // MOTIF_RANGE, ZNORM_THRESHOLD);
    // System.out.println(motifsBF);

    MotifRecord motifsEMMA = EMMAImplementation.series2EMMAMotifs(series, MOTIF_SIZE, MOTIF_RANGE,
        PAA_SIZE, ALPHABET_SIZE, ZNORM_THRESHOLD);
    Date end = new Date();

    System.out.println(motifsEMMA);

    System.out.println("done in " + SAXProcessor.timeToString(start.getTime(), end.getTime()));
  }
}
