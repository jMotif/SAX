package net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import net.seninp.jmotif.sax.datastructure.DoublyLinkedSortedList;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.util.JmotifMapEntry;

public class TestDoublyLinkedSortedList {

  private Comparator<Entry<String, SAXRecord>> cAscending;
  private Comparator<Entry<String, SAXRecord>> cDescending;
  private SAXRecord srLargest;
  private Entry<String, SAXRecord> entryLargest;
  private Entry<String, SAXRecord> entryMed;
  private Entry<String, SAXRecord> entrySmallest;

  @Before
  public void setUp() throws Exception {

    cAscending = new Comparator<Entry<String, SAXRecord>>() {
      @Override
      public int compare(Entry<String, SAXRecord> o1, Entry<String, SAXRecord> o2) {
        int f1 = o1.getValue().getIndexes().size();
        int f2 = o2.getValue().getIndexes().size();
        return Integer.compare(f2, f1);
      }
    };

    cDescending = new Comparator<Entry<String, SAXRecord>>() {
      @Override
      public int compare(Entry<String, SAXRecord> o1, Entry<String, SAXRecord> o2) {
        int f1 = o1.getValue().getIndexes().size();
        int f2 = o2.getValue().getIndexes().size();
        return Integer.compare(f1, f2);
      }
    };

    srLargest = new SAXRecord(String.valueOf("aaa").toCharArray(), 10);
    srLargest.addIndex(11);
    srLargest.addIndex(12);
    entryLargest = new JmotifMapEntry<String, SAXRecord>(String.valueOf(srLargest.getPayload()),
        srLargest);

    SAXRecord srMed = new SAXRecord(String.valueOf("bbb").toCharArray(), 15);
    srMed.addIndex(16);
    entryMed = new JmotifMapEntry<String, SAXRecord>(String.valueOf(srMed.getPayload()), srMed);

    SAXRecord srSmallest = new SAXRecord(String.valueOf("ccc").toCharArray(), 20);
    entrySmallest = new JmotifMapEntry<String, SAXRecord>(String.valueOf(srSmallest.getPayload()),
        srSmallest);
  }

  @Test
  public void testAddElement() {

    DoublyLinkedSortedList<Entry<String, SAXRecord>> testList = new DoublyLinkedSortedList<>(2,
        cAscending);
    assertTrue("Assert emptiness", testList.isEmpty());

    testList.addElement(entryLargest);

    assertFalse("Assert non-emptiness", testList.isEmpty());

    Entry<String, SAXRecord> e = testList.iterator().next();

    assertTrue("assert add element success", e.getKey().equals(entryLargest.getKey()));
  }

  @Test
  public void testComparatorAscending() {

    DoublyLinkedSortedList<Entry<String, SAXRecord>> testListA = new DoublyLinkedSortedList<>(2,
        cAscending);
    testListA.addElement(entrySmallest);
    testListA.addElement(entryLargest);
    testListA.addElement(entryMed);

    Iterator<Entry<String, SAXRecord>> i = testListA.iterator();

    Entry<String, SAXRecord> e = i.next();
    assertTrue("assert add element success", e.getKey().equals(entrySmallest.getKey()));

    e = i.next();
    assertTrue("assert add element success", e.getKey().equals(entryMed.getKey()));

    assertFalse("assert the list size", i.hasNext());

  }

  @Test
  public void testComparatorDescending() {

    DoublyLinkedSortedList<Entry<String, SAXRecord>> testListD = new DoublyLinkedSortedList<>(3,
        cDescending);
    testListD.addElement(entrySmallest);
    testListD.addElement(entryLargest);
    testListD.addElement(entryMed);

    Iterator<Entry<String, SAXRecord>> i = testListD.iterator();

    Entry<String, SAXRecord> e = i.next();
    assertTrue("assert add element success", e.getKey().equals(entryLargest.getKey()));

    e = i.next();
    assertTrue("assert add element success", e.getKey().equals(entryMed.getKey()));

    e = i.next();
    assertTrue("assert add element success", e.getKey().equals(entrySmallest.getKey()));

  }

}
