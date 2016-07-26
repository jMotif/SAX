package net.seninp.jmotif.sax.bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;

public class Shingles {

  private Map<String, List<int[]>> shingles;
  private HashMap<String, Integer> indexTable;

  /**
   * Constructor. Creates all the data structures.
   * 
   * @param alphabetSize the expected alphabet size.
   * @param shingleSize the expected shingle size.
   */
  public Shingles(int alphabetSize, int shingleSize) {

    shingles = new HashMap<String, List<int[]>>();

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

  public HashMap<String, Integer> getIndex() {
    return this.indexTable;
  }

  public void addShingledSeries(String key, int[] counts) {
    if (null == shingles.get(key)) {
      shingles.put(key, new ArrayList<int[]>());
    }
    shingles.get(key).add(counts);
  }

  public List<int[]> get(String key) {
    return shingles.get(key);
  }

  public int indexForShingle(String shingle) {
    return this.indexTable.get(shingle);
  }

}
