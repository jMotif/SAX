package net.seninp.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The sorted array list implementation following the SO question #4031572.
 * 
 * @see <a href="http://stackoverflow.com/questions/4031572/sorted-array-list-in-java.">http://
 * stackoverflow.com/questions/4031572/sorted-array-list-in-java.</a>
 * 
 * 
 * @author psenin
 * 
 * @param <T> the type.
 */
public class SortedArrayList<T> extends ArrayList<T> {

  /** The fancy serial. */
  private static final long serialVersionUID = 291265617765342218L;

  /**
   * Inserts an element and sorts the array.
   * 
   * @param value the value to insert.
   */
  public void insertSorted(T value) {
    add(value);
    @SuppressWarnings("unchecked")
    Comparable<T> cmp = (Comparable<T>) value;
    for (int i = size() - 1; i > 0 && cmp.compareTo(get(i - 1)) < 0; i--) {
      Collections.swap(this, i, i - 1);
    }
  }
}