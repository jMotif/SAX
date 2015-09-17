package net.seninp.jmotif.sax.discord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import net.seninp.util.StackTrace;

public class TestDiscordDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int WIN_SIZE = 100;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.01;

  private static final int DISCORDS_TO_TEST = 3;

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
          WIN_SIZE, DISCORDS_TO_TEST, new LargeWindowAlgorithm());

      discordsTrie = HOTSAXImplementation.series2Discords(series, DISCORDS_TO_TEST, WIN_SIZE,
          ALPHABET_SIZE, new LargeWindowAlgorithm(), NumerosityReductionStrategy.NONE,
          NORM_THRESHOLD);

      discordsHash = HOTSAXImplementation.series2DiscordsWithHash(series, DISCORDS_TO_TEST,
          WIN_SIZE, PAA_SIZE, ALPHABET_SIZE, new LargeWindowAlgorithm(),
          NumerosityReductionStrategy.NONE, NORM_THRESHOLD);

    }
    catch (Exception e) {
      fail("sholdn't throw an exception, exception thrown: \n" + StackTrace.toString(e));
      e.printStackTrace();
    }

    for (int i = 0; i < DISCORDS_TO_TEST; i++) {

      Double d1 = discordsBruteForce.get(i).getNNDistance();
      Double d2 = discordsHash.get(i).getNNDistance();
      Double d3 = discordsTrie.get(i).getNNDistance();

      assertEquals(d1, d2);
      assertEquals(d1, d3);

    }

  }
}
