package net.seninp.jmotif.sax.tinker;

import java.io.Console;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.discord.HOTSAXImplementation;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;

public class DiscordDiscoveryPerformanceNew {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/300_signal1.txt";

  private static final int WIN_SIZE = 300;
  private static final int PAA_SIZE = 4;
  private static final int ALPHABET_SIZE = 4;

  private static final double NORM_THRESHOLD = 0.01;

  private static final int DISCORDS_TO_TEST = 3;

  private static double[] series;

  public static void main(String[] args) throws Exception {

    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);

    Console c = System.console();
    if (c != null) {
      c.format("\nPress ENTER to proceed.\n");
      c.readLine();
    }

    DiscordRecords discordsHash = HOTSAXImplementation.series2Discords(series, DISCORDS_TO_TEST,
        WIN_SIZE, PAA_SIZE, ALPHABET_SIZE, NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
    for (DiscordRecord d : discordsHash) {
      System.out.println("hotsax hash discord " + d.toString());
    }

    c = System.console();
    if (c != null) {
      c.format("\nPress ENTER to proceed.\n");
      c.readLine();
    }

    DiscordRecords discordsBruteForce = BruteForceDiscordImplementation
        .series2BruteForceDiscords(series, WIN_SIZE, DISCORDS_TO_TEST, new LargeWindowAlgorithm());
    for (DiscordRecord d : discordsBruteForce) {
      System.out.println("brute force discord " + d.toString());
    }

  }
}
