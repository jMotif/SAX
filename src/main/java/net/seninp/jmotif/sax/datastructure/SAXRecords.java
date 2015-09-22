package net.seninp.jmotif.sax.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The collection for SAXRecords. This datastructure is used in the parallel SAX implementation.
 * 
 * @author psenin
 * 
 */
// public class SAXRecords {
public class SAXRecords implements Iterable<SAXRecord> {

  /** The id is used to identify the chunk. */
  private final long id;

  /** All the SAX records. */
  private final HashMap<String, SAXRecord> records;

  /** The index of occurrences, key is the position in the time series. */
  private final HashMap<Integer, SAXRecord> realTSindex;

  /** The mapping from SAX string positions to real time series positions. */
  private HashMap<Integer, Integer> stringPosToRealPos;

  /**
   * Constructor. The structure id will be set as System.currentTimeMillis().
   */
  public SAXRecords() {
    super();
    this.id = System.currentTimeMillis();
    this.records = new HashMap<String, SAXRecord>();
    this.realTSindex = new HashMap<Integer, SAXRecord>();
  }

  /**
   * The recommended constructor.
   * 
   * @param id The structure id.
   */
  public SAXRecords(long id) {
    super();
    this.id = id;
    this.records = new HashMap<String, SAXRecord>();
    this.realTSindex = new HashMap<Integer, SAXRecord>();
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
   * Returns an iterator which is backed by a hash map with no guarantee for ordering.
   * 
   * @return an iterator.
   */
  @Override
  public Iterator<SAXRecord> iterator() {
    return this.records.values().iterator();
  }

  /**
   * Gets an entry by the index.
   * 
   * @param idx The index.
   * @return The entry.
   */
  public SAXRecord getByIndex(int idx) {
    return realTSindex.get(idx);
  }

  /**
   * Get a SAX record by the string key.
   * 
   * @param str The query string.
   * @return the record if exists.
   */
  public SAXRecord getByWord(String str) {
    return records.get(str);
  }

  /**
   * Drops a single entry.
   * 
   * @param idx the index.
   */
  public void dropByIndex(int idx) {
    SAXRecord entry = realTSindex.get(idx);
    if (null != entry) {
      realTSindex.remove(idx);
      entry.removeIndex(idx);
      if (entry.getIndexes().isEmpty()) {
        records.remove(String.valueOf(entry.getPayload()));
      }
    }
  }

  /**
   * Adds a single string and index entry by creating a SAXRecord.
   * 
   * @param str The string.
   * @param idx The index.
   */
  public void add(char[] str, int idx) {
    SAXRecord rr = records.get(String.valueOf(str));
    if (null == rr) {
      rr = new SAXRecord(str, idx);
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
    for (SAXRecord record : records) {
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
  public ArrayList<Integer> getAllIndices() {
    ArrayList<Integer> res = new ArrayList<Integer>(this.realTSindex.size());
    res.addAll(this.realTSindex.keySet());
    Collections.sort(res);
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
        SAXRecord rec = realTSindex.get(p);
        rec.removeIndex(p);
        if (rec.getIndexes().isEmpty()) {
          this.records.remove(String.valueOf(rec.getPayload()));
        }
        realTSindex.remove(p);
      }
    }
  }

  /**
   * Get motifs.
   * 
   * @param num how many motifs to report.
   * @return the array of motif SAXRecords.
   */
  public ArrayList<SAXRecord> getMotifs(int num) {
    ArrayList<SAXRecord> res = new ArrayList<SAXRecord>(num);
    DoublyLinkedSortedList<Entry<String, SAXRecord>> list = new DoublyLinkedSortedList<Entry<String, SAXRecord>>(
        num, new Comparator<Entry<String, SAXRecord>>() {
          @Override
          public int compare(Entry<String, SAXRecord> o1, Entry<String, SAXRecord> o2) {
            int f1 = o1.getValue().getIndexes().size();
            int f2 = o2.getValue().getIndexes().size();
            return Integer.compare(f1, f2);
          }
        });
    for (Entry<String, SAXRecord> e : this.records.entrySet()) {
      list.addElement(e);
    }
    Iterator<Entry<String, SAXRecord>> i = list.iterator();
    while (i.hasNext()) {
      res.add(i.next().getValue());
    }
    return res;
  }

  /**
   * Get access to records.
   * 
   * @return records.
   */
  public Collection<SAXRecord> getRecords() {
    return this.records.values();
  }

}
