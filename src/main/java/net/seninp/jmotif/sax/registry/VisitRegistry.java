package net.seninp.jmotif.sax.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * The convenient way to keep track of visited locations.
 * 
 * @author Pavel Senin.
 */
public class VisitRegistry implements Cloneable {

  protected byte[] registry; // 1 visited, 0 unvisited
  private int unvisitedCount;
  private final Random randomizer = new Random(System.currentTimeMillis());
  private int capacity;

  /**
   * Constructor.
   * 
   * @param capacity The initial capacity.
   */
  public VisitRegistry(int capacity) {
    super();
    this.capacity = capacity;
    this.registry = new byte[capacity];
    this.unvisitedCount = capacity;
  }

  /**
   * Disabling the default constructor.
   */
  @SuppressWarnings("unused")
  private VisitRegistry() {
    super();
  }

  /**
   * Mark as visited certain location.
   * 
   * @param i The location to mark.
   */
  public void markVisited(int i) {
    if (i >= 0 && i < this.capacity && 0 == this.registry[i]) {
      this.unvisitedCount--;
      this.registry[i] = 1;
    }
  }

  /**
   * Marks as visited a range of locations.
   * 
   * @param start the start of labeling (inclusive).
   * @param end the end of labeling (exclusive).
   */
  public void markVisited(int start, int end) {
    for (int i = start; i < end; i++) {
      this.markVisited(i);
    }
  }

  /**
   * Get the next random unvisited position.
   * 
   * @return The next unvisited position.
   */
  public int getNextRandomUnvisitedPosition() {
    // if all visited return -1
    if (0 == this.unvisitedCount) {
      return -1;
    }
    // if there is space continue with random sampling
    int i = this.randomizer.nextInt(capacity);
    while (1 == registry[i]) {
      i = this.randomizer.nextInt(capacity);
    }
    return i;
  }

  /**
   * Check if position is not visited.
   * 
   * @param i The index.
   * @return true if not visited.
   */
  public boolean isNotVisited(int i) {
    return (0 == this.registry[i]);
  }

  /**
   * Check if interval boundaries were visited.
   * 
   * @param intervalStart The interval start (inclusive).
   * @param intervalEnd The interval end (inclusive).
   * @return True if visited.
   */
  public boolean isVisited(Integer intervalStart, int intervalEnd) {
    // do a bit of validation here and signal about error
    //
    if (intervalStart < 0) {
      throw new RuntimeException("In the registry logic asked to look left from 0!!!");
    }
    else if (intervalEnd >= this.registry.length) {
      throw new RuntimeException("In the registry logic asked to look beyond the right margin "
          + this.registry.length + "!!!");
    }
    return (1 == this.registry[intervalStart] || 1 == this.registry[intervalEnd]);
  }

  /**
   * Get the list of unvisited positions.
   * 
   * @return list of unvisited positions.
   */
  public ArrayList<Integer> getUnvisited() {
    ArrayList<Integer> res = new ArrayList<Integer>(capacity);
    for (int i = 0; i < capacity; i++) {
      if (0 == this.registry[i]) {
        res.add(i);
      }
    }
    return res;
  }

  /**
   * Get the list of visited positions.
   * 
   * @return list of visited positions.
   */
  public int[] getVisited() {
    int count = 0;
    for (int i = 0; i < capacity; i++) {
      if (1 == this.registry[i]) {
        count++;
      }
    }
    int[] res = new int[count];
    int cp = 0;
    for (int i = 0; i < capacity; i++) {
      if (1 == this.registry[i]) {
        res[cp] = i;
        cp++;
      }
    }
    return res;
  }

  /**
   * Transfers all visited entries from another registry to current.
   * 
   * @param discordRegistry The discords registry to copy from.
   */
  public void transferVisited(VisitRegistry discordRegistry) {
    for (int v : discordRegistry.getVisited()) {
      this.markVisited(v);
    }
  }

  /**
   * Creates the copy of a registry.
   * 
   * @return the complete copy.
   * @throws CloneNotSupportedException if error occurs.
   */
  public VisitRegistry clone() throws CloneNotSupportedException {
    VisitRegistry res = (VisitRegistry) super.clone();
    res.capacity = this.capacity;
    res.unvisitedCount = this.unvisitedCount;
    res.registry = Arrays.copyOfRange(this.registry, 0, this.registry.length);
    return res;
  }

  /**
   * The registry size.
   * 
   * @return the registry size.
   */
  public int size() {
    return this.registry.length;
  }

}
