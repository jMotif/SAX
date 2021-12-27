package net.seninp.jmotif.sax.discord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import net.seninp.util.StackTrace;

public class TestDiscordDiscoveryNONE {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int WIN_SIZE = 100;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.01;

  private static final int DISCORDS_TO_TEST = 5;

  private static final NumerosityReductionStrategy STRATEGY = NumerosityReductionStrategy.NONE;

  // logging stuff
  //
  private static final Logger LOGGER;
  private static final Level LOGGING_LEVEL = Level.INFO;

  static {
    LOGGER = (Logger) LoggerFactory.getLogger(TestDiscordDiscoveryNONE.class);
    LOGGER.setLevel(LOGGING_LEVEL);
  }

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    series = Arrays.copyOf(series, 800);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void test() {

    DiscordRecords discordsBruteForce = null;
    DiscordRecords discordsHash = null;
    DiscordRecords discordsOle = null;

    try {

      discordsBruteForce = BruteForceDiscordImplementation.series2BruteForceDiscords(series,
          WIN_SIZE, DISCORDS_TO_TEST, new LargeWindowAlgorithm(), NORM_THRESHOLD);
      for (DiscordRecord d : discordsBruteForce) {
        LOGGER.debug("brute force discord " + d.toString());
      }

      discordsHash = HOTSAXImplementation.series2Discords(series, DISCORDS_TO_TEST, WIN_SIZE,
          PAA_SIZE, ALPHABET_SIZE, STRATEGY, NORM_THRESHOLD);
      for (DiscordRecord d : discordsHash) {
        LOGGER.debug("hotsax hash discord " + d.toString());
      }

      discordsOle = HOTSAXImplementation.series2DiscordsDeprecated(series, DISCORDS_TO_TEST,
          WIN_SIZE, PAA_SIZE, ALPHABET_SIZE, new LargeWindowAlgorithm(), STRATEGY, NORM_THRESHOLD);
      for (DiscordRecord d : discordsOle) {
        LOGGER.debug("old hash discord " + d.toString());
      }

    }
    catch (Exception e) {
      fail("shouldn't throw an exception, exception thrown: \n" + StackTrace.toString(e));
      e.printStackTrace();
    }

    for (int i = 0; i < DISCORDS_TO_TEST; i++) {

      Double d1 = discordsBruteForce.get(i).getNNDistance();
      Double d2 = discordsHash.get(i).getNNDistance();
      Double d3 = discordsOle.get(i).getNNDistance();

      assertEquals(WIN_SIZE, discordsBruteForce.get(i).getLength());
      assertEquals(WIN_SIZE, discordsHash.get(i).getLength());
      assertEquals(WIN_SIZE, discordsOle.get(i).getLength());

      assertEquals(d1, d2);
      assertEquals(d1, d3);

    }

  }
}
