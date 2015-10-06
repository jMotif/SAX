package net.seninp.jmotif.util;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import net.seninp.util.UCRUtils;

public class TestUCRUtils {

  private static final String CBF_FNAME = "src//resources//dataset/CBF/CBF_TRAIN";

  @Test
  public void testReadUCRData() {
    try {

      Map<String, List<double[]>> data = UCRUtils.readUCRData(CBF_FNAME);
      assertEquals(data.keySet().size(), 3);

      String stats = UCRUtils.datasetStats(data, "CBF_TRAIN ");
      assertTrue(stats.contains("series length min: 128, max: 128"));
    }
    catch (IOException e) {
      fail("Exception should not be thrown");
    }
  }

}
