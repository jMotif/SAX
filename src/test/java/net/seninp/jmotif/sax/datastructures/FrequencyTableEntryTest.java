package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.seninp.jmotif.sax.datastructure.FrequencyTableEntry;

/**
 * Test frequency table entry.
 * 
 * @author psenin
 *
 */
public class FrequencyTableEntryTest {

  @Test
  public void testFrequencyTableEntryIntInt() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, 11);
    final FrequencyTableEntry fe12 = new FrequencyTableEntry(10, 11);
    final FrequencyTableEntry fe2 = new FrequencyTableEntry(19, 77);

    assertTrue(fe1.equals(fe12));
    assertFalse(fe1.equals(fe2));

    assertEquals(fe1.hashCode(), fe12.hashCode());
    assertNotEquals(fe1.hashCode(), fe2.hashCode());

    assertTrue(fe1.equals(fe12));
    assertFalse(fe1.equals(fe2));

  }

  @Test
  public void testFrequencyTableEntryIntegerCharArrayInt() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, "abcd".toCharArray(), 11);
    final FrequencyTableEntry fe12 = new FrequencyTableEntry(10, "abcd".toCharArray(), 11);
    final FrequencyTableEntry fe2 = new FrequencyTableEntry(19, "abcd".toCharArray(), 77);

    assertTrue(fe1.equals(fe12));
    assertFalse(fe1.equals(fe2));

    assertEquals(fe1.hashCode(), fe12.hashCode());
    assertNotEquals(fe1.hashCode(), fe2.hashCode());

    assertTrue(fe1.equals(fe12));
    assertFalse(fe1.equals(fe2));

  }

  @Test
  public void testFrequencyTableEntrySettersGetters() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, "abcd".toCharArray(), 11);
    final FrequencyTableEntry fe12 = new FrequencyTableEntry(10, "abcd".toCharArray(), 11);
    final FrequencyTableEntry fe2 = new FrequencyTableEntry(19, "abcd".toCharArray(), 77);

    assertEquals(fe1.getFrequency(), fe12.getFrequency());
    assertNotEquals(fe1.getFrequency(), fe2.getFrequency());

    assertEquals(fe1.getPosition(), fe12.getPosition());
    assertNotEquals(fe1.getPosition(), fe2.getPosition());

    assertTrue(String.valueOf(fe1.getStr()).equals(String.valueOf(fe12.getStr())));
    assertTrue(String.valueOf(fe1.getStr()).equals(String.valueOf(fe2.getStr())));

    fe2.setStr("bbbb".toCharArray());
    assertNotEquals(fe1.getStr(), fe2.getStr());

    assertTrue(fe2.isTrivial(0));
    assertTrue(fe1.isTrivial(5));
    assertFalse(fe1.isTrivial(2));

  }

}
