package net.seninp.jmotif.sax;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import net.seninp.util.StackTrace;

/**
 * Tests the Stack Trace class.
 * 
 * @author Philip Johnson
 */
public class TestSAXException {

  /**
   * Tests the Exception thrown. Generates an exception, makes the Stack Trace, and checks to see if
   * it seems OK.
   */
  @Test
  public void testStackTrace() {
    String trace;
    try {
      throw new SAXException("Test Exception");
    }
    catch (Exception e) {
      trace = StackTrace.toString(e);
    }
    assertTrue("Check trace", trace.startsWith("net.seninp.jmotif.sax.SAXException"));

    trace = null;
    try {
      throw new SAXException("Test Exception", new Throwable("for the valid reason"));
    }
    catch (Exception e) {
      trace = StackTrace.toString(e);
    }
    assertTrue("Check trace", trace.startsWith("net.seninp.jmotif.sax.SAXException"));
  }

}