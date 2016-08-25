package net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class TestNormalAlphabet {

  private NormalAlphabet a;

  @Before
  public void setUp() throws Exception {
    a = new NormalAlphabet();
  }

  @Test
  public void testGetMaxSize() {
    assertEquals(a.getMaxSize(), Integer.valueOf(20));
  }

  @Test
  public void testGetCuts() {
    try {
      for (int i = 2; i < a.getMaxSize(); i++) {
        assertTrue(a.getCuts(i).length > 0);
      }
    }
    catch (SAXException e) {
      fail("Should not throw exception");
    }

    try {
      @SuppressWarnings("unused")
      double[] dd = a.getCuts(1);
      fail("Should throw an exception");
    }
    catch (SAXException e) {
      assert true;
    }

    try {
      @SuppressWarnings("unused")
      double[] dd = a.getCuts(21);
      fail("Should throw an exception");
    }
    catch (SAXException e) {
      assert true;
    }

  }

  @Test
  public void testGetDistanceMatrix() {

    try {
      for (int i = 2; i < a.getMaxSize(); i++) {
        assertTrue(a.getDistanceMatrix(i).length > 0);
      }
    }
    catch (SAXException e) {
      fail("Should not throw exception");
    }

    try {
      @SuppressWarnings("unused")
      double[][] dd = a.getDistanceMatrix(1);
      fail("Should throw an exception");
    }
    catch (SAXException e) {
      assert true;
    }

    try {
      @SuppressWarnings("unused")
      double[][] dd = a.getDistanceMatrix(21);
      fail("Should throw an exception");
    }
    catch (SAXException e) {
      assert true;
    }

  }

  @Test
  public void testException() {

    try {
      @SuppressWarnings("unused")
      double[] cuts1 = a.getCuts(1);
      fail("exception is not thrown");
    }
    catch (SAXException e) {
      assert true;
    }

    try {
      @SuppressWarnings("unused")
      double[] cuts1 = a.getCentralCuts(1);
      fail("exception is not thrown");
    }
    catch (SAXException e) {
      assert true;
    }

    try {
      @SuppressWarnings("unused")
      double[] cuts1 = a.getCuts(1);
      fail("exception is not thrown");
    }
    catch (SAXException e) {
      assert true;
    }

    try {
      @SuppressWarnings("unused")
      double[][] dist1 = a.getDistanceMatrix(1);
      fail("exception is not thrown");
    }
    catch (SAXException e) {
      assert true;
    }

  }

  @Test
  public void testVarCutsAndDistances() {

    try {
      for (int i = 2; i < 21; i++) {
        double[] cuts;
        cuts = a.getCuts(i);
        assertEquals(i - 1, cuts.length);
      }
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }

    try {
      for (int i = 2; i < 21; i++) {
        double[] cuts;
        cuts = a.getCentralCuts(i);
        assertEquals(i, cuts.length);
      }
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }

    try {
      for (int i = 2; i < 21; i++) {
        double[][] dist = a.getDistanceMatrix(i);
        assertEquals(i, (dist[0]).length);
      }
    }
    catch (SAXException e) {
      fail("exception shall not be thrown!");
    }

  }

}
