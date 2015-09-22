package net.seninp.jmotif.sax.registry;

public class MagicArrayEntry implements Cloneable, Comparable<MagicArrayEntry> {

  protected String word;
  protected int freq;

  public MagicArrayEntry(String payload, int frequency) {
    this.word = payload;
    this.freq = frequency;
  }

  @Override
  public int compareTo(MagicArrayEntry arg0) {
    if (null == arg0) {
      throw new NullPointerException("Unable to compare with a null object.");
    }
    if (this.freq > arg0.freq) {
      return 1;
    }
    else if (this.freq < arg0.freq) {
      return -1;
    }
    return 0;
  }

  public String getStr() {
    return this.word;
  }

}
