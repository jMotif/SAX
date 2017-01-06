package net.seninp.jmotif.sax.bitmap;

import java.util.HashMap;
import java.util.Map;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;

/**
 * A container for shingled data. Accepts a time series label and its SAX decomposition.
 * 
 * @author psenin
 *
 */
public class Shingles {

  private Map<String, int[]> shingles;

  private HashMap<String, Integer> indexTable;

  /**
   * Constructor. Creates all the data structures.
   * 
   * @param alphabetSize the expected alphabet size.
   * @param shingleSize the expected shingle size.
   */
  public Shingles(int alphabetSize, int shingleSize) {

    shingles = new HashMap<String, int[]>();

    indexTable = new HashMap<String, Integer>();

    // build all shingles index
    //
    String[] alphabet = new String[alphabetSize];
    for (int i = 0; i < alphabetSize; i++) {
      alphabet[i] = String.valueOf(TSProcessor.ALPHABET[i]);
    }
    String[] allStrings = SAXProcessor.getAllPermutations(alphabet, shingleSize);

    // and make an index table
    //
    for (int i = 0; i < allStrings.length; i++) {
      indexTable.put(allStrings[i], i);
    }

  }

  /**
   * Returns the index of shingles.
   * 
   * @return the mapping of the vector elements indices to a certain shingle string.
   */
  public HashMap<String, Integer> getShinglesIndex() {
    return this.indexTable;
  }

  /**
   * Adds a shingled time series to the table -- the user responsible for the proper ordering.
   * 
   * @param key the shingle array label.
   * @param counts the counts array.
   */
  public void addShingledSeries(String key, int[] counts) {
    shingles.put(key, counts);
  }

  /**
   * Adds a shingled series assuring the proper index.
   * 
   * @param key
   * @param shingledSeries
   */
  public void addShingledSeries(String key, Map<String, Integer> shingledSeries) {
    // allocate the weights array corresponding to the time series
    int[] counts = new int[this.indexTable.size()];
    // fill in the counts
    for (String str : shingledSeries.keySet()) {
      Integer idx = this.indexTable.get(str);
      if (null == idx) {
        throw new IndexOutOfBoundsException("the requested shingle " + str + " doesn't exist!");
      }
      counts[idx] = shingledSeries.get(str);
    }
    shingles.put(key, counts);
  }

  /**
   * get a shingles frequency array for the key.
   * 
   * @param key
   * @return
   */
  public int[] get(String key) {
    return shingles.get(key);
  }

  /**
   * Get shingles.
   * 
   * @return
   * 
   * @return
   */
  public Map<String, int[]> getShingles() {
    return this.shingles;
  }

}
