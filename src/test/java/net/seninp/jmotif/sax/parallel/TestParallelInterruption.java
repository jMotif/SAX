package net.seninp.jmotif.sax.parallel;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;

public class TestParallelInterruption {

  private static double[] ts;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    InputStream fileStream = new FileInputStream("src/resources/dataset/300_signal1.txt.gz");
    InputStream gzipStream = new GZIPInputStream(fileStream);
    Reader decoder = new InputStreamReader(gzipStream, "US-ASCII");
    BufferedReader buffered = new BufferedReader(decoder);
    ts = TSProcessor.readTS(buffered, 0, 10000);
  }

  @Test
  public void test() {
    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    try {
      SAXRecords res = ps.process(ts, 2, 50, 5, 5, NumerosityReductionStrategy.NONE, 0.005);
      Thread.sleep(1);
      ps.cancel();
    }
    catch (Exception e) {
      fail("Shouldn't throw any exception");
    }
  }

}
