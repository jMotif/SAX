package net.seninp.jmotif.sax.datastructure;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.seninp.util.StackTrace;

/**
 * Implement Iterable doubly linked list.
 * 
 * @author psenin
 * 
 * @param <T> the elemnt type.
 */
public class DoublyLinkedSortedList<T> {

  private class MyIterator implements Iterator<T> {

    private DoublyLinkedSortedList<T> list;
    private Node<T> current;

    public MyIterator(DoublyLinkedSortedList<T> doublyLinkedSortedList) {
      this.list = doublyLinkedSortedList;
    }

    @Override
    public boolean hasNext() {
      if (null == this.current) {
        if (this.list.isEmpty()) {
          return false;
        }
        return true;
      }
      if (null == this.current.next) {
        return false;
      }
      return true;
    }

    @Override
    public T next() {
      try {
        if (null == current) {
          current = this.list.first;
          return current.data;
        }
        current = current.next;
        return current.data;
      }
      catch (Exception e) {
        throw new NoSuchElementException(
            "There was an exception thrown: " + StackTrace.toString(e));
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("The remove is unsupported.");
    }
  }

  // private classes should be at the end of the file
  private static class Node<T> {
    protected T data;
    protected Node<T> next;
    @SuppressWarnings("unused")
    protected Node<T> prev;

    public Node(T data) {
      this.data = data;
    }

    @Override
    public String toString() {
      return data.toString();
    }

  }

  private int maxSize;
  private Comparator<T> comparator;

  private Node<T> first = null;
  private int size = 0;

  /**
   * Constructor.
   * 
   * @param listSize elements that falls off the list are discarded.
   * @param comparator the comparator.
   */
  public DoublyLinkedSortedList(int listSize, Comparator<T> comparator) {
    this.maxSize = listSize;
    this.comparator = comparator;
  }

  /**
   * Adds a data instance.
   * 
   * @param data the data to put in the list.
   */
  public void addElement(T data) {

    Node<T> newNode = new Node<T>(data);

    if (isEmpty()) {
      //
      // if it is the very first node in the list
      first = newNode;
      size = 1;
    }
    else {

      // if this node is greater than the list's head
      //
      if (this.comparator.compare(newNode.data, first.data) > 0) {
        Node<T> tmp = first;
        first = newNode;
        first.next = tmp;
        tmp.prev = first;
        size++;
      }
      else {

        Node<T> prev = first;
        Node<T> current = first.next;

        while (current != null) {

          // if this node is the current node less than the new one
          //
          if (this.comparator.compare(newNode.data, current.data) > 0) {
            prev.next = newNode;
            newNode.prev = prev;
            current.prev = newNode;
            newNode.next = current;
            size++;
            break;
          }
          current = current.next;
          prev = prev.next;

        }

        // if all list elements are greater than the new one
        //
        if (null == current) {
          prev.next = newNode;
          newNode.prev = prev;
          size++;
        }

        if (size > this.maxSize) {
          dropLastElement();
        }

      }
    }

  }

  private void dropLastElement() {
    if (this.size >= 2) {
      Node<T> current = first;
      while (current.next.next != null) {
        current = current.next;
      }
      current.next.prev = null;
      current.next = null;
      this.size--;
    }
  }

  public Iterator<T> iterator() {
    return new MyIterator(this);
  }

  public boolean isEmpty() {
    return (first == null);
  }
}