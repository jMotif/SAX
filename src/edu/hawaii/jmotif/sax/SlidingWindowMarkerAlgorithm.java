package edu.hawaii.jmotif.sax;


public interface SlidingWindowMarkerAlgorithm {

  /**
   * Marks visited locations (of the magic array).
   * 
   * @param registry The magic array instance.
   * @param startPosition The position to start labeling from.
   * @param intervalLength The length of the interval to be labeled.
   */
  void markVisited(VisitRegistry registry, int startPosition, int intervalLength);

}
