package net.seninp.jmotif.sax.discord;

public class MagicArrayEntry implements Cloneable, Comparable<MagicArrayEntry> {

  protected int pos;
  protected String word;
  protected int freq;

  public MagicArrayEntry(Integer location, String payload, int frequency) {
    this.pos = location;
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

  public int getPosition() {
    return this.pos;
  }

  public String getStr() {
    return this.word;
  }

}
