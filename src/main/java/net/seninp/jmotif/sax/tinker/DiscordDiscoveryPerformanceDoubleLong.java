package net.seninp.jmotif.sax.tinker;

import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.discord.HOTSAXImplementation;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;

public class DiscordDiscoveryPerformanceDoubleLong {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int WIN_SIZE = 120;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.5;

  private static final int DISCORDS_TO_TEST = 5;

  private static double[] series;

  public static void main(String[] args) throws Exception {

    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);

    double[] tmp = new double[series.length * 2];
    for (int i = 0; i < series.length; i++) {
      tmp[i] = series[i];
      tmp[i + series.length] = series[i];
    }
    series = tmp;

    DiscordRecords discordsBruteForce = null;
    DiscordRecords discordsHash = null;

    discordsBruteForce = BruteForceDiscordImplementation.series2BruteForceDiscords(series, WIN_SIZE,
        DISCORDS_TO_TEST, new LargeWindowAlgorithm(), NORM_THRESHOLD);
    for (DiscordRecord d : discordsBruteForce) {
      System.out.println("brute force discord " + d.toString());
    }

    discordsHash = HOTSAXImplementation.series2Discords(series, DISCORDS_TO_TEST, WIN_SIZE,
        PAA_SIZE, ALPHABET_SIZE, NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
    for (DiscordRecord d : discordsHash) {
      System.out.println("hotsax hash discord " + d.toString());
    }

  }
}
