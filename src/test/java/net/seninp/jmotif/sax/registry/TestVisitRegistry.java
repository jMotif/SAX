package net.seninp.jmotif.sax.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the visit registry.
 * 
 * @author psenin
 * 
 */
public class TestVisitRegistry {

  private static final int REG_SIZE = 177;
  private static final int RANDOM_VISITS = 13;

  private VisitRegistry vr;

  /**
   * Set up.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    vr = new VisitRegistry(REG_SIZE);
  }

  /**
   * Test the constructor.
   */
  @Test
  public void testInitCounters() {

    assertEquals("Test visit registry", REG_SIZE, vr.size());
    assertEquals("Test visit registry", REG_SIZE, vr.getUnvisited().size());
    assertTrue("Test visit registry", vr.getVisited().isEmpty());

  }

  /**
   * Test the point marker.
   */
  @Test
  public void testMarkPointVisited() {

    assertEquals("Test visit registry", REG_SIZE, vr.size());

    ArrayList<Integer> shallBeVisited = new ArrayList<Integer>(RANDOM_VISITS);

    // mark 13 positions randomly
    for (int i = 0; i < RANDOM_VISITS; i++) {
      int pos = vr.getNextRandomUnvisitedPosition();
      shallBeVisited.add(pos);
      vr.markVisited(pos);
    }

    assertEquals("Test visit registry", REG_SIZE - RANDOM_VISITS, vr.getUnvisited().size());
    assertEquals("Test visit registry", RANDOM_VISITS, vr.getVisited().size());

    // check they are marked
    for (int pos : shallBeVisited) {
      assertFalse(vr.isNotVisited(pos));
      assertTrue(vr.isVisited(pos));
    }

    // check queries are OK
    ArrayList<Integer> visited = vr.getVisited();
    for (int pos : visited) {
      assertTrue(shallBeVisited.contains(pos));
    }
    ArrayList<Integer> unVisited = vr.getUnvisited();
    for (int pos : unVisited) {
      assertFalse(shallBeVisited.contains(pos));
    }

    // test the transfer right here
    //
    VisitRegistry vrTransfered = new VisitRegistry(REG_SIZE);
    vrTransfered.transferVisited(vr);
    for (Integer pos : vrTransfered.getVisited()) {
      assertTrue(shallBeVisited.contains(pos));
    }
    for (int pos : vrTransfered.getUnvisited()) {
      assertFalse(shallBeVisited.contains(pos));
    }
  }

  /**
   * Test the interval marker.
   */
  @Test
  public void testMarkIntervalVisited() {

    int mark1Start = 3;
    int mark1End = 29;

    int mark2Start = vr.size() / 2;
    int mark2End = vr.size() - 17;

    vr.markVisited(mark1Start, mark1End);
    vr.markVisited(mark2Start, mark2End);

    // check first marker
    for (int i = 0; i < mark1Start; i++) {
      assertTrue(vr.isNotVisited(i));
      assertFalse(vr.isVisited(i));
    }
    for (int i = mark1Start; i < mark1End; i++) {
      assertFalse(vr.isNotVisited(i));
      assertTrue(vr.isVisited(i));
    }
    for (int i = mark1End; i < mark2Start; i++) {
      assertTrue(vr.isNotVisited(i));
      assertFalse(vr.isVisited(i));
    }

    // check second marker
    for (int i = mark2Start; i < mark2End; i++) {
      assertFalse(vr.isNotVisited(i));
      assertTrue(vr.isVisited(i));
    }
    for (int i = mark2End; i < vr.size(); i++) {
      assertTrue(vr.isNotVisited(i));
      assertFalse(vr.isVisited(i));
    }

    // check the interval checker
    assertTrue(vr.isVisited(mark1Start, mark1End));
    assertTrue(vr.isVisited(mark1Start - 1, mark1End));
    assertTrue(vr.isVisited(mark1Start, mark1End + 1));
  }

  /**
   * Test the interval marker.
   */
  @Test
  public void testRandomizer() {

    HashSet<Integer> seq = new HashSet<Integer>();

    int i = -1;
    while (-1 != (i = vr.getNextRandomUnvisitedPosition())) { // outer loop
      vr.markVisited(i);
      seq.add(i);
    }

    assertEquals(REG_SIZE, vr.getVisited().size());
    assertEquals(seq.size(), vr.getVisited().size());
    assertTrue(vr.getUnvisited().isEmpty());

  }

  /**
   * Test bounds.
   */
  @Test
  public void testBounds() {

    try {
      vr.markVisited(-3);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.markVisited(REG_SIZE + 3);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.markVisited(-3, 1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.markVisited(1, REG_SIZE + 1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.isVisited(REG_SIZE + 1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.isVisited(-1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.isNotVisited(-1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.isVisited(2, REG_SIZE + 1);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }

    try {
      vr.isVisited(-2, REG_SIZE);
      fail("exception was not thrown!");
    }
    catch (RuntimeException e) {
      assert true;
    }
  }

}
