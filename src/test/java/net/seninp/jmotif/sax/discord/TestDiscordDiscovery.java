package net.seninp.jmotif.sax.discord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import org.junit.Before;
import org.junit.Test;

public class TestDiscordDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int WIN_SIZE = 100;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.01;

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
  }

  @Test
  public void test() {
    DiscordRecords discordsBruteForce = null;
    DiscordRecords discordsTrie = null;
    DiscordRecords discordsHash = null;
    try {

      discordsBruteForce = BruteForceDiscordImplementation.series2BruteForceDiscords(series,
          WIN_SIZE, 2, new LargeWindowAlgorithm());

      discordsTrie = HOTSAXImplementation.series2Discords(series, WIN_SIZE, ALPHABET_SIZE, 2,
          new LargeWindowAlgorithm(), NORM_THRESHOLD);

      discordsHash = HOTSAXImplementation.series2DiscordsWithHash(series, WIN_SIZE, PAA_SIZE,
          ALPHABET_SIZE, 2, new LargeWindowAlgorithm(), NORM_THRESHOLD);
    }
    catch (Exception e) {
      fail("sholdn throw an exception");
      e.printStackTrace();
    }

    assertEquals("discords test", discordsBruteForce.get(0).getPosition(), discordsHash.get(0)
        .getPosition());

    assertEquals("discords test", discordsHash.get(0).getPosition(), discordsTrie.get(0)
        .getPosition());

  }
}
