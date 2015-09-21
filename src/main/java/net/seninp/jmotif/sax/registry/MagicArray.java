package net.seninp.jmotif.sax.registry;

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Random;

/**
 * A magic array to track visits.
 * 
 * @author psenin
 *
 */
public class MagicArray {

  private Hashtable<Integer, boolean[]> registry;
  // private int[] index;

  private int locallyUnvisitedCount;
  @SuppressWarnings("unused")
  private int globallyUnvisitedCount;

  private int[] randomVisitArray;
  private int unvisitedIndex;

  // private final Random randomizer = new Random(System.currentTimeMillis());

  public MagicArray(Hashtable<Integer, boolean[]> res) {
    this.registry = res;
    // this.index = new int[res.size()];
    // int i = 0;
    // for (Entry<Integer, boolean[]> e : res.entrySet()) {
    // this.index[i] = e.getKey();
    // i++;
    // }
    // Arrays.sort(this.index);
    this.locallyUnvisitedCount = this.registry.size();
    this.globallyUnvisitedCount = this.registry.size();
  }

  public boolean isGloballyVisited(int loc) {
    return registry.get(loc)[0];
  }

  public boolean isLocallyVisited(int loc) {
    return registry.get(loc)[1];
  }

  public void markGloballyVisited(int loc) {
    if (!(registry.get(loc)[0])) {
      this.globallyUnvisitedCount--;
      registry.get(loc)[0] = true;
    }
  }

  public void markGloballyVisited(int start, int end) {
    for (int i = start; i < end; i++) {
      if (registry.containsKey(i)) {
        if (!(registry.get(i)[0])) {
          this.registry.get(i)[0] = true;
          this.globallyUnvisitedCount--;
        }
      }
    }
  }

  public void markLocallyVisited(int loc) {
    if (!(registry.get(loc)[1])) {
      registry.get(loc)[1] = true;
      this.locallyUnvisitedCount--;
    }
  }

  public void markLocallyVisited(int start, int end) {
    for (int i = start; i < end; i++) {
      if (registry.containsKey(i)) {
        if (!(registry.get(i)[1])) {
          this.registry.get(i)[1] = true;
          this.locallyUnvisitedCount--;
        }
      }
    }
  }

  public void resetLocal() {
    for (Entry<Integer, boolean[]> e : this.registry.entrySet()) {
      e.getValue()[1] = false;
    }
    this.locallyUnvisitedCount = this.registry.size();
  }

  /**
   * Generates a next unvisited candidate.
   * 
   * @return
   */
  public int getNextLocallyUnvisitedPosition() {

    if (0 == this.locallyUnvisitedCount) {
      return -1;
    }

    this.unvisitedIndex++;
    return this.randomVisitArray[unvisitedIndex - 1];

  }

  public void RandomLocalSearchRedify() {
    this.randomVisitArray = new int[this.locallyUnvisitedCount];
    int ctr = 0;
    for (Entry<Integer, boolean[]> e : this.registry.entrySet()) {
      if (e.getValue()[1]) {
        continue;
      }
      else {
        this.randomVisitArray[ctr] = e.getKey();
        ctr++;
      }
    }
    shuffle(this.randomVisitArray);
    this.unvisitedIndex = 0;
  }

  private void shuffle(int[] array) {
    Random rnd = new Random();
    for (int i = array.length - 1; i > 0; i--) {
      int index = rnd.nextInt(i + 1);
      int a = array[index];
      array[index] = array[i];
      array[i] = a;
    }
  }

}
