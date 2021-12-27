package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import net.seninp.jmotif.sax.datastructure.FrequencyTableEntry;

/**
 * Test frequency table entry.
 * 
 * @author psenin
 *
 */
public class TestFrequencyTableEntry {

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

    fe1.setFrequency(fe2.getFrequency());
    assertEquals(fe1.getFrequency(), fe2.getFrequency());

  }

  @Test
  public void testToStringAndColne() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, "abcd".toCharArray(), 11);

    FrequencyTableEntry fe_copy = fe1.copy();
    assertTrue(fe1.toString().equalsIgnoreCase(fe_copy.toString()));

    int newPos = 22;
    fe_copy.setPosition(newPos);
    assertEquals(newPos, fe_copy.getPosition());

  }

  @Test
  public void testFrequencyTableEntryComplexity() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, "a".toCharArray(), 11);

    assertTrue(fe1.isTrivial(null));
    assertTrue(fe1.isTrivial(5));

    fe1.setStr("aaaaaa".toCharArray());
    assertTrue(fe1.isTrivial(5));

  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testFrequencyTableEntryCmp() {

    final FrequencyTableEntry fe1 = new FrequencyTableEntry(10, "a".toCharArray(), 11);

    try {
      fe1.compareTo(null);
      fail("Exception was not thrown!");
    }
    catch (NullPointerException e) {
      assert true;
    }

    assertTrue(fe1.equals(fe1));
    assertFalse(fe1.equals(null));
    assertFalse(fe1.equals(Integer.valueOf(5)));

  }
}
