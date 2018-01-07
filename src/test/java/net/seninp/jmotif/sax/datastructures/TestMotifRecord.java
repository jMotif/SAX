package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;
import net.seninp.jmotif.sax.motif.MotifRecord;

public class TestMotifRecord {

  @Test
  public void testMotifRecord() {
    MotifRecord mr = new MotifRecord(1, new ArrayList<Integer>());
    assertSame("Testing constructor", 1, mr.getLocation());
    assertTrue("Testing constructor", mr.getOccurrences().isEmpty());
    assertSame("Testing constructor", 0, mr.getFrequency());
  }

  @Test
  public void testSetLocation() {
    MotifRecord mr = new MotifRecord(1, new ArrayList<Integer>());
    mr.setLocation(77);
    assertSame("Testing the setter", 77, mr.getLocation());
  }

  @Test
  public void testGetFrequency() {
    MotifRecord mr = new MotifRecord(0, new ArrayList<Integer>());
    assertSame("Testing freqs", 0, mr.getFrequency());
    mr.add(1);
    assertSame("Testing freqs", 1, mr.getFrequency());
    assertTrue("Testing freqs", mr.getOccurrences().contains(1));
    mr.add(77);
    assertSame("Testing freqs", 77, mr.getFrequency());
    assertTrue("Testing freqs", mr.getOccurrences().contains(77));

    assertSame("Testing freqs", 2, mr.getOccurrences().size());
  }

  @Test
  public void testToString() {
    MotifRecord mr = new MotifRecord(0, new ArrayList<Integer>());
    mr.add(1);
    mr.add(2);
    assertNotNull("Testing to string", mr.toString());
  }

}
