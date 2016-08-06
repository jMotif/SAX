package net.seninp.util;

import java.util.Map;

/**
 * Implements a map entry. Used for custom data structures.
 * 
 * @author psenin
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class JmotifMapEntry<K, V> implements Map.Entry<K, V> {
  private final K key;
  private V value;

  public JmotifMapEntry(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public K getKey() {
    return key;
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public V setValue(V value) {
    V old = this.value;
    this.value = value;
    return old;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    @SuppressWarnings("rawtypes")
    JmotifMapEntry other = (JmotifMapEntry) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    }
    else if (!key.equals(other.key))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    }
    else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JmotifMapEntry [key=" + key + ", value=" + value + "]";
  }

}
