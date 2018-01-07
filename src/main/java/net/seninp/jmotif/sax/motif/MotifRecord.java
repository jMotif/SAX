package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Keeps motifs organized.
 * 
 * @author psenin
 *
 */
public class MotifRecord {

  private int location;
  private TreeSet<Integer> occurrences;

  /**
   * Constructor.
   * 
   * @param motifLiocation the motif location.
   * @param motifOccurrences occurrence locations.
   */
  public MotifRecord(int motifLiocation, ArrayList<Integer> motifOccurrences) {
    this.location = motifLiocation;
    this.occurrences = new TreeSet<Integer>();
    this.occurrences.addAll(motifOccurrences);
  }

  /**
   * The location setter.
   * 
   * @param location the motif location.
   */
  public void setLocation(int location) {
    this.location = location;
  }

  /**
   * The location getter.
   * 
   * @return the motif location.
   */
  public int getLocation() {
    return location;
  }

  /**
   * Gets the occurrence frequency.
   * 
   * @return the motif occurrence frequency (itself isnt included).
   */
  public int getFrequency() {
    return this.occurrences.size();
  }

  /**
   * The occurrences array (copy) getter.
   * 
   * @return motif occurrences.
   */
  public ArrayList<Integer> getOccurrences() {
    ArrayList<Integer> res = new ArrayList<Integer>(this.occurrences.size());
    for (Integer e : this.occurrences) {
      res.add(e);
    }
    return res;
  }

  /**
   * The location setter.
   * 
   * @param newLocation the motif location.
   */
  public void add(int newLocation) {
    if (!this.occurrences.contains(newLocation)) {
      this.occurrences.add(newLocation);
    }
  }

  @Override
  public String toString() {
    return "MotifRecord [location=" + this.location + ", freq=" + this.occurrences.size()
        + ", occurrences=" + occurrences + "]";
  }

}
