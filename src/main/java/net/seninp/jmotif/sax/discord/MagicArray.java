package net.seninp.jmotif.sax.discord;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Map.Entry;

public class MagicArray {

  private Hashtable<Integer, boolean[]> registry;
  private int[] index;
  private final Random randomizer = new Random(System.currentTimeMillis());
  private int locallyUnvisitedCount;
  @SuppressWarnings("unused")
  private int globallyUnvisitedCount;

  public MagicArray(Hashtable<Integer, boolean[]> res) {
    this.registry = res;
    this.index = new int[res.size()];
    int i = 0;
    for (Entry<Integer, boolean[]> e : res.entrySet()) {
      this.index[i] = e.getKey();
      i++;
    }
    Arrays.sort(this.index);
    this.locallyUnvisitedCount = this.index.length;
    this.globallyUnvisitedCount = this.index.length;
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
    for (int i : this.index) {
      registry.get(i)[1] = false;
    }
    this.locallyUnvisitedCount = this.index.length;
  }

  public int getNextLocallyUnvisitedPosition() {

    if (0 == this.locallyUnvisitedCount) {
      return -1;
    }

    int next = randomizer.nextInt(this.index.length);
    int idx = this.index[next];
    int saveidx = idx;

    if (this.registry.get(idx)[1]) {

      int direction = randomizer.nextInt(2);

      if (0 == direction && next >= 0) {
        while ((true == this.registry.get(idx)[1]) && next > 0) {
          next = next - 1;
          idx = this.index[next];
        }
        if (-1 == next) { // should move up
          idx = saveidx + 1;
          while ((true == this.registry.get(idx)[1]) && next < this.index.length - 1) {
            next = next + 1;
            idx = this.index[next];
          }
        }
        return idx;

      }
      else if (1 == direction && next < this.index.length) {
        while ((true == this.registry.get(idx)[1]) && next < this.index.length - 1) {
          next = next + 1;
          idx = this.index[next];
        }
        if (this.index.length == next) { // should move down
          idx = saveidx - 1;
          while ((true == this.registry.get(idx)[1]) && next > 0) {
            next = next - 1;
            idx = this.index[next];
          }
        }
        return idx;
      }
    }
    else {
      return idx;
    }

    return 0;
  }

}
