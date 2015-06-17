package net.seninp.jmotif.sax.algorithm;

import static org.junit.Assert.*;
import net.seninp.jmotif.sax.discord.BruteForceDiscord;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import net.seninp.util.StackTrace;
import net.seninp.util.SAXData;
import org.junit.Before;
import org.junit.Test;

public class TestDiscordFinder {

  private BruteForceDiscord df;

  @Before
  public void setUp() throws Exception {
    df = new BruteForceDiscord();
  }

  @Test
  public void test() {
    VisitRegistry visitRegistry = new VisitRegistry(SAXData.ecg0606.length);
    try {
      DiscordRecord discord = df.findBestDiscordBruteForce(SAXData.ecg0606, 100, visitRegistry,
          new LargeWindowAlgorithm());
      assertEquals(discord.getPosition(), 411);
    }
    catch (Exception e) {
      fail("Exception was thrown: " + StackTrace.toString(e));
    }
  }

}
