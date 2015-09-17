package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class TestMotifDiscovery {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";
  private static final Alphabet na = new NormalAlphabet();

  private static final int WIN_SIZE = 100;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.EXACT;

  private SAXRecords saxData;

  @Before
  public void setUp() throws Exception {
    double[] series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    SAXProcessor sp = new SAXProcessor();
    saxData = sp.ts2saxViaWindow(series, WIN_SIZE, PAA_SIZE, na.getCuts(ALPHABET_SIZE),
        NR_STRATEGY, NORM_THRESHOLD);
  }

  @Test
  public void test() {
    String bestRec = null;
    int maxFreq = Integer.MIN_VALUE;
    for (SAXRecord e : saxData) {
      int f = e.getIndexes().size();
      if (f > maxFreq) {
        bestRec = String.valueOf(e.getPayload());
        maxFreq = f;
      }
    }

    ArrayList<SAXRecord> motifs = saxData.getMotifs(10);
    assertTrue("assert top motif", bestRec.equals(String.valueOf(motifs.get(0).getPayload())));
  }
}
