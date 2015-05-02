package net.seninp.jmotif.sax.algorithm;

import static org.junit.Assert.*;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import net.seninp.util.StackTrace;
import net.seninp.util.TestData;
import org.junit.Before;
import org.junit.Test;

public class TestDiscordFinder {

  private DiscordFinder df;

  @Before
  public void setUp() throws Exception {
    df = new DiscordFinder();
  }

  @Test
  public void test() {
    VisitRegistry visitRegistry = new VisitRegistry(TestData.ecg0606.length);
    try {
      DiscordRecord discord = df.findBestDiscordBruteForce(TestData.ecg0606, 100, visitRegistry,
          new LargeWindowAlgorithm());
      assertEquals(discord.getPosition(), 411);
    }
    catch (Exception e) {
      fail("Exception was thrown: " + StackTrace.toString(e));
    }
  }

}
