package net.seninp.jmotif.sax.datastructure;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class FrequencyTableEntry implements Comparable<FrequencyTableEntry> {

  private int position;
  private char[] payload;
  private int frequency;

  /**
   * Constructor.
   * 
   * @param len the length of the string.
   * @param pos the occurrence position.
   */
  public FrequencyTableEntry(int len, int pos) {
    this.payload = new char[len];
    this.frequency = -1;
    this.position = pos;
  }

  /**
   * Constructor.
   * 
   * @param position the original entry position.
   * @param payload the payload string.
   * @param frequency the frequency.
   */
  public FrequencyTableEntry(Integer position, char[] payload, int frequency) {
    this.position = position;
    this.payload = payload;
    this.frequency = frequency;
  }

  /**
   * Get a string payload.
   * 
   * @return a string payload.
   */
  public char[] getStr() {
    char[] res = new char[this.payload.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = this.payload[i];
    }
    return res;
  }

  /**
   * Set a string payload.
   * 
   * @param str the string payload.
   */
  public void setStr(char[] str) {
    this.payload = Arrays.copyOf(str, str.length);
  }

  /**
   * Get the observed frequency of the word.
   * 
   * @return the observed frequency of the word.
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * Set the observed frequency of the word.
   * 
   * @param frequency The frequency to set.
   */
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  /**
   * Get the position of the word.
   * 
   * @return the position of the word.
   */
  public int getPosition() {
    return position;
  }

  /**
   * Set the position of the word.
   * 
   * @param position The position to set.
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * Check the complexity of the string.
   * 
   * @param complexity If 1 - single letter used, 2 - two or more letters, 3 - 3 or more etc..
   * 
   * @return Returns true if complexity conditions are met.
   */
  public boolean isTrivial(Integer complexity) {
    int len = payload.length;
    if ((null == complexity) || (len < 2)) {
      return true;
    }
    else if ((complexity.intValue() > 0) && (len > 2)) {
      Set<Character> seen = new TreeSet<Character>();
      for (int i = 0; i < len; i++) {
        Character c = Character.valueOf(this.payload[i]);
        if (seen.contains(c)) {
          continue;
        }
        else {
          seen.add(c);
        }
      }
      if (complexity.intValue() <= seen.size()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int compareTo(FrequencyTableEntry arg0) {
    if (null == arg0) {
      throw new NullPointerException("Unable to compare with a null object.");
    }
    if (this.frequency > arg0.getFrequency()) {
      return 1;
    }
    else if (this.frequency < arg0.getFrequency()) {
      return -1;
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + frequency;
    result = prime * result + Arrays.hashCode(payload);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FrequencyTableEntry other = (FrequencyTableEntry) obj;
    if (frequency != other.frequency) {
      return false;
    }
    if (!Arrays.equals(payload, other.payload)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return "payload: " + String.valueOf(this.payload) + ", frequency: " + this.frequency
        + ", location: " + this.position;
  }

  /**
   * Makes the instance copy.
   * 
   * @return the object copy (a "clone").
   */
  public FrequencyTableEntry copy() {
    FrequencyTableEntry res = new FrequencyTableEntry(this.position, this.payload, this.frequency);
    return res;
  }

}
