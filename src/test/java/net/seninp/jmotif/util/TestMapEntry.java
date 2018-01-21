package net.seninp.jmotif.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import net.seninp.util.JmotifMapEntry;

public class TestMapEntry {

  private static final String KEY1 = "key1";
  private static final String KEY2 = "key2";
  private static final Integer KEY3 = 75;

  private static final String VALUE1 = "value1";
  private static final String VALUE2 = "value2";
  private static final Double VALUE3 = Double.valueOf(12.77d);

  private JmotifMapEntry<String, String> e1;
  private JmotifMapEntry<String, String> e2;
  private JmotifMapEntry<Integer, Double> e3;
  private JmotifMapEntry<String, String> e4;

  /**
   * Test set-up.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    e1 = new JmotifMapEntry<String, String>(KEY1, VALUE1);
    e2 = new JmotifMapEntry<String, String>(KEY1, VALUE1);
    e3 = new JmotifMapEntry<Integer, Double>(KEY3, VALUE3);
    e4 = new JmotifMapEntry<String, String>(KEY2, VALUE2);

  }

  @Test
  public void testHashCode() {
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotEquals(e1.hashCode(), e3.hashCode());
  }

  @Test
  public void testGetKey() {
    assertEquals(e1.getKey(), KEY1);
    assertEquals(e3.getKey(), KEY3);
    assertNotEquals(e1.getKey(), KEY3);
  }

  @Test
  public void testGetValue() {
    assertEquals(e1.getValue(), VALUE1);
    assertEquals(e3.getValue(), VALUE3);
    assertNotEquals(e1.getValue(), VALUE3);
  }

  @Test
  public void testSetValue() {
    e2.setValue(VALUE1);
    assertEquals(e2.getValue(), VALUE1);
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEqualsObject() {

    JmotifMapEntry<String, String> e11 = new JmotifMapEntry<String, String>(KEY1, VALUE1);

    assertTrue(e11.equals(e1));
    assertTrue(e1.equals(e2));

    assertTrue(e1.equals(e1));
    assertFalse(e1.equals(null));
    assertFalse(e1.equals(Integer.valueOf(17)));
    assertFalse((new JmotifMapEntry<String, String>(null, null)).equals(e1));
    assertFalse((new JmotifMapEntry<String, String>("key", null)).equals(e1));
    assertFalse((new JmotifMapEntry<String, Double>(KEY1, VALUE3)).equals(e1));

    // System.out.println(e11.equals(e4));

    assertFalse(e11.equals(e4));
  }

  @Test
  public void testToString() {
    assertTrue(e1.toString().contains(VALUE1));
    assertTrue(e1.toString().contains(KEY1));
    assertFalse(e1.toString().contains(VALUE2));
  }

}
