package net.seninp.jmotif.sax.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * The convenient way to keep track of visited locations. Note that a new unvisited location is
 * searched for by random generator-backed search. which may take time... sometimes :).
 * 
 * @author Pavel Senin.
 */
public class VisitRegistry implements Cloneable {

  private static final byte ZERO = 0;
  private static final byte ONE = 1;

  protected byte[] registry; // 1 visited, 0 unvisited

  private int unvisitedCount; // unvisited counter

  private final Random randomizer = new Random(System.currentTimeMillis());

  /**
   * Constructor.
   * 
   * @param capacity The initial capacity.
   */
  public VisitRegistry(int capacity) {
    super();
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
   * Marks location visited. If it was unvisited, counter decremented.
   * 
   * @param loc The location to mark.
   */
  public void markVisited(int loc) {
    if (checkBounds(loc)) {
      if (ZERO == this.registry[loc]) {
        this.unvisitedCount--;
      }
      this.registry[loc] = ONE;
    }
    else {
      throw new RuntimeException(
          "The location " + loc + " out of bounds [0," + (this.registry.length - 1) + "]");
    }
  }

  /**
   * Marks as visited a range of locations.
   * 
   * @param from the start of labeling (inclusive).
   * @param upTo the end of labeling (exclusive).
   */
  public void markVisited(int from, int upTo) {
    if (checkBounds(from) && checkBounds(upTo - 1)) {
      for (int i = from; i < upTo; i++) {
        this.markVisited(i);
      }
    }
    else {
      throw new RuntimeException("The location " + from + "," + upTo + " out of bounds [0,"
          + (this.registry.length - 1) + "]");
    }
  }

  /**
   * Get the next random unvisited position.
   * 
   * @return The next unvisited position.
   */
  public int getNextRandomUnvisitedPosition() {

    // if all are visited, return -1
    //
    if (0 == this.unvisitedCount) {
      return -1;
    }

    // if there is space continue with random sampling
    //
    int i = this.randomizer.nextInt(this.registry.length);
    while (ONE == registry[i]) {
      i = this.randomizer.nextInt(this.registry.length);
    }
    return i;
  }

  /**
   * Check if position is not visited.
   * 
   * @param loc The index.
   * @return true if not visited.
   */
  public boolean isNotVisited(int loc) {
    if (checkBounds(loc)) {
      return (ZERO == this.registry[loc]);
    }
    else {
      throw new RuntimeException(
          "The location " + loc + " out of bounds [0," + (this.registry.length - 1) + "]");
    }
  }

  /**
   * Check if the interval and its boundaries were visited.
   * 
   * @param from The interval start (inclusive).
   * @param upTo The interval end (exclusive).
   * @return True if visited.
   */
  public boolean isVisited(int from, int upTo) {
    if (checkBounds(from) && checkBounds(upTo - 1)) {
      // perform the visit check
      //
      for (int i = from; i < upTo; i++) {
        if (ONE == this.registry[i]) {
          return true;
        }
      }
      return false;
    }
    else {
      throw new RuntimeException("The location " + from + "," + upTo + " out of bounds [0,"
          + (this.registry.length - 1) + "]");
    }
  }

  /**
   * Check if the location specified is visited.
   * 
   * @param loc the location.
   * @return true if visited
   */
  public boolean isVisited(int loc) {
    if (checkBounds(loc)) {
      return (ONE == this.registry[loc]);
    }
    else {
      throw new RuntimeException(
          "The location " + loc + " out of bounds [0," + (this.registry.length - 1) + "]");
    }

  }

  /**
   * Get the list of unvisited positions.
   * 
   * @return list of unvisited positions.
   */
  public ArrayList<Integer> getUnvisited() {
    if (0 == this.unvisitedCount) {
      return new ArrayList<Integer>();
    }
    ArrayList<Integer> res = new ArrayList<Integer>(this.unvisitedCount);
    for (int i = 0; i < this.registry.length; i++) {
      if (ZERO == this.registry[i]) {
        res.add(i);
      }
    }
    return res;
  }

  /**
   * Get the list of visited positions. Returns NULL if none are visited.
   * 
   * @return list of visited positions.
   */
  public ArrayList<Integer> getVisited() {
    if (0 == (this.registry.length - this.unvisitedCount)) {
      return new ArrayList<Integer>();
    }
    ArrayList<Integer> res = new ArrayList<Integer>(this.registry.length - this.unvisitedCount);
    for (int i = 0; i < this.registry.length; i++) {
      if (ONE == this.registry[i]) {
        res.add(i);
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

  /**
   * Check the bounds.
   * 
   * @param pos the pos to check.
   * @return true if within the bounds.
   */
  private boolean checkBounds(int pos) {
    if (pos < 0 || pos >= this.registry.length) {
      return false;
    }
    return true;
  }

  public int getUnvisitedCount() {
    return this.unvisitedCount;
  }

}
