package net.seninp.jmotif.sax.motif;

import java.util.ArrayList;

/**
 * Keeps motifs organized.
 * 
 * @author psenin
 *
 */
public class MotifRecord {

  private double location;
  private int count;
  private ArrayList<Integer> occurrences;

  /**
   * Constructor.
   * 
   * @param motifLiocation the motif location.
   * @param motifCount frequency of occurrence.
   * @param motifOccurrences occurrence locations.
   */
  public MotifRecord(double motifLiocation, int motifCount, ArrayList<Integer> motifOccurrences) {
    this.location = motifLiocation;
    this.count = motifCount;
    this.occurrences = motifOccurrences;
  }

}
