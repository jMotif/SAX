package net.seninp.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The collection for SAXRecords. This datastructure is used in the parallel SAX implementation.
 * 
 * @author psenin
 * 
 */
// public class SAXRecords {
public class SAXRecords implements Iterable<SaxRecord> {

  /** The id is used to identify the chunk. */
  private final long id;

  /** All the SAX records. */
  private final HashMap<String, SaxRecord> records;

  /** The index of occurrences, key is the position in the time series. */
  private final HashMap<Integer, SaxRecord> realTSindex;

  /** The mapping from SAX string positions to real time series positions. */
  private HashMap<Integer, Integer> stringPosToRealPos;

  /**
   * Disable this.
   */
  public SAXRecords() {
    super();
    this.id = System.currentTimeMillis();
    this.records = new HashMap<String, SaxRecord>();
    this.realTSindex = new HashMap<Integer, SaxRecord>();
  }

  /**
   * The recommended constructor.
   * 
   * @param id The structure id.
   */
  public SAXRecords(long id) {
    super();
    this.id = id;
    this.records = new HashMap<String, SaxRecord>();
    this.realTSindex = new HashMap<Integer, SaxRecord>();
  }

  /**
   * Get the id.
   * 
   * @return the id.
   */
  public long getId() {
    return this.id;
  }

  /**
   * Returns an iterator which is backed by a hash map (i.e. there is no guarantee for keys
   * ordering).
   * 
   * @return an iterator.
   */
  @Override
  public Iterator<SaxRecord> iterator() {
    return this.realTSindex.values().iterator();
  }

  /**
   * Gets an entry by the index.
   * 
   * @param idx The index.
   * @return The entry.
   */
  public SaxRecord getByIndex(int idx) {
    return realTSindex.get(idx);
  }

  /**
   * Get a SAX record by the string key.
   * 
   * @param str The query string.
   * @return the record if exists.
   */
  public SaxRecord getByWord(String str) {
    return records.get(str);
  }

  /**
   * Drops a single entry.
   * 
   * @param idx the index.
   */
  public void dropByIndex(int idx) {
    SaxRecord entry = realTSindex.get(idx);
    if (null == entry) {
      return;
    }
    // how many things in da index
    if (1 == entry.getIndexes().size()) {
      // dropping the entry completely off
      realTSindex.remove(idx);
      records.remove(String.valueOf(entry.getPayload()));
    }
    else {
      // dropping off just a single index
      entry.removeIndex(idx);
      realTSindex.remove(idx);
    }
  }

  /**
   * Adds a single string and index entry by creating a SAXRecord.
   * 
   * @param str The string.
   * @param idx The index.
   */
  public void add(char[] str, int idx) {
    SaxRecord rr = records.get(String.valueOf(str));
    if (null == rr) {
      rr = new SaxRecord(str, idx);
      this.records.put(String.valueOf(str), rr);
    }
    else {
      rr.addIndex(idx);
    }
    this.realTSindex.put(idx, rr);
  }

  /**
   * Adds all entries from the collection.
   * 
   * @param records The collection.
   */
  public void addAll(SAXRecords records) {
    for (SaxRecord record : records) {
      char[] payload = record.getPayload();
      for (Integer i : record.getIndexes()) {
        this.add(payload, i);
      }
    }
  }

  /**
   * This adds all to the internal store. Used by the parallel SAX conversion engine.
   * 
   * @param records the data to add.
   */
  public void addAll(HashMap<Integer, char[]> records) {
    for (Entry<Integer, char[]> e : records.entrySet()) {
      this.add(e.getValue(), e.getKey());
    }
  }

  /**
   * Finds the minimal index value.
   * 
   * @return the minimal index value.
   */
  public int getMinIndex() {
    return Collections.min(this.realTSindex.keySet());
  }

  /**
   * Finds the maximal index value.
   * 
   * @return the maximal index value.
   */
  public int getMaxIndex() {
    return Collections.max(this.realTSindex.keySet());
  }

  /**
   * Get the collection size in indexes.
   * 
   * @return the collection size in indexes.
   */
  public int size() {
    return this.realTSindex.size();
  }

  /**
   * Get all the indexes.
   * 
   * @return all the indexes.
   */
  public Set<Integer> getIndexes() {
    return this.realTSindex.keySet();
  }

  /**
   * Get the SAX string of this whole collection.
   * 
   * @param separatorToken The separator token to use for the string.
   * 
   * @return The whole data as a string.
   */
  public String getSAXString(String separatorToken) {
    StringBuffer sb = new StringBuffer();
    ArrayList<Integer> index = new ArrayList<Integer>();
    index.addAll(this.realTSindex.keySet());
    Collections.sort(index, new Comparator<Integer>() {
      public int compare(Integer int1, Integer int2) {
        return int1.compareTo(int2);
      }
    });
    for (int i : index) {
      sb.append(this.realTSindex.get(i).getPayload()).append(separatorToken);
    }
    return sb.toString();
  }

  /**
   * Get all indexes, sorted.
   * 
   * @return all the indexes.
   */
  public SortedSet<Integer> getAllIndices() {
    SortedSet<Integer> res = new TreeSet<Integer>();
    res.addAll(this.realTSindex.keySet());
    return res;
  }

  /**
   * This builds an index that aids in mapping of a SAX word to the real timeseries index.
   */
  public void buildIndex() {
    this.stringPosToRealPos = new HashMap<Integer, Integer>();
    int counter = 0;
    for (Integer idx : getAllIndices()) {
      this.stringPosToRealPos.put(counter, idx);
      counter++;
    }
  }

  /**
   * This maps an index of the word in the output string to the real position in time-series.
   * 
   * @param idx the index to map.
   * @return the position in the time-series.
   */
  public Integer mapStringIndexToTSPosition(int idx) {
    return this.stringPosToRealPos.get(idx);
  }

  /**
   * Removes saxRecord occurrences that correspond to these positions.
   * 
   * @param positions The positions to clear.
   */
  public void excludePositions(ArrayList<Integer> positions) {
    for (Integer p : positions) {
      if (realTSindex.containsKey(p)) {
        SaxRecord rec = realTSindex.get(p);
        rec.removeIndex(p);
        if (0 == rec.getIndexes().size()) {
          this.records.remove(String.valueOf(rec.getPayload()));
        }
        realTSindex.remove(p);
      }
    }
  }

}
