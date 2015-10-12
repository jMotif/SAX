package net.seninp.jmotif.sax.registry;

public class MagicArrayEntry implements Comparable<MagicArrayEntry> {

  protected String word;
  protected int freq;

  public MagicArrayEntry(String payload, int frequency) {
    this.word = payload;
    this.freq = frequency;
  }

  @Override
  public int compareTo(MagicArrayEntry arg0) {
    if (arg0 == null) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + freq;
    result = prime * result + ((word == null) ? 0 : word.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MagicArrayEntry other = (MagicArrayEntry) obj;
    if (freq != other.freq)
      return false;
    if (word == null) {
      if (other.word != null)
        return false;
    }
    else if (!word.equals(other.word))
      return false;
    return true;
  }

}
