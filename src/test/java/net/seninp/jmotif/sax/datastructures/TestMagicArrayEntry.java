package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.*;
import org.junit.Test;
import net.seninp.jmotif.sax.registry.MagicArrayEntry;

public class TestMagicArrayEntry {

  private static final String PAYLOAD1 = "aaa";
  private static final int FREQUENCY1 = 2;

  private static final String PAYLOAD2 = "bbb";
  private static final int FREQUENCY2 = 7;

  @Test
  public void testHashCode() {
    MagicArrayEntry entry1 = new MagicArrayEntry(PAYLOAD1, FREQUENCY1);
    MagicArrayEntry entry2 = new MagicArrayEntry(PAYLOAD1, FREQUENCY1);

    assertTrue("testing hash function", entry1.hashCode() == entry2.hashCode());

    entry2 = new MagicArrayEntry(PAYLOAD2, FREQUENCY1);
    assertFalse("testing hash function", entry1.hashCode() == entry2.hashCode());

    entry2 = new MagicArrayEntry(PAYLOAD1, FREQUENCY2);
    assertFalse("testing hash function", entry1.hashCode() == entry2.hashCode());
  }

  @Test
  public void testCompareTo() {
    MagicArrayEntry entry1 = new MagicArrayEntry(PAYLOAD1, FREQUENCY1);
    MagicArrayEntry entry2 = new MagicArrayEntry(PAYLOAD1, FREQUENCY1);

    assertTrue("testing hash function", entry1.equals(entry2));
    assertTrue("testing hash function", entry1.equals(entry1));

    assertFalse("testing hash function", entry1.equals(null));
    assertFalse("testing hash function", entry1.equals(new Integer(7)));

    entry2 = new MagicArrayEntry(PAYLOAD2, FREQUENCY1);
    assertFalse("testing hash function", entry1.equals(entry2));

    entry2 = new MagicArrayEntry(PAYLOAD1, FREQUENCY2);
    assertFalse("testing hash function", entry1.equals(entry2));
  }

}
