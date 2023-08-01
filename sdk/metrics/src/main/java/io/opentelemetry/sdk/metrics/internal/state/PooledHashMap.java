/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A bucket-based hash map with an internal re-usable map entry objects pool
 * <p>
 * The goal of this map is to minimize memory allocation, leading to reduced time spent
 * in garbage collection.
 * <p>
 * This map avoids allocating a new map entry on each put operation by maintaining a pool
 * of reusable (mutable) map entries and borrowing a map entry object from the pool to hold
 * the given key-value of the put operation. The borrowed object is returned to the pool
 * when the map entry key is removed from the map.
 *
 * @param <K> The map key type
 * @param <V> The map value type
 */
public class PooledHashMap<K, V> implements Map<K, V> {
  private static final int DEFAULT_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;

  private ArrayList<Entry<K, V>>[] table;
  private final ObjectPool<Entry<K, V>> entryPool;
  private int size;
//  private final EntrySetView entrySetView;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public PooledHashMap(int capacity) {
    this.table = new ArrayList[capacity];
    this.entryPool = new ObjectPool<>(Entry::new);
    this.size = 0;
//    entrySetView = new EntrySetView();
  }

  public PooledHashMap() {
    this(DEFAULT_CAPACITY);
  }

  @Override
  @Nullable
  public V put(K key, V value) {
    Objects.requireNonNull(key, "This map does not support null keys");
    Objects.requireNonNull(key, "This map does not support null values");
    if (size > LOAD_FACTOR * table.length) {
      rehash();
    }

    int bucket = getBucket(key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries == null) {
      entries = new ArrayList<>();
      table[bucket] = entries;
    } else {
      for (int i = 0; i < entries.size(); i++) {
        Entry<K, V> entry = entries.get(i);
        if (Objects.equals(entry.key, key)) {
          V oldValue = entry.value;
          entry.value = value;
          return oldValue;
        }
      }
    }
    Entry<K, V> entry = entryPool.borrowObject();
    entry.key = key;
    entry.value = value;
    entries.add(entry);
    size++;
    return null;
  }

  @SuppressWarnings({"rawtypes", "unchecked", "NullAway"})
  private void rehash() {
    ArrayList<Entry<K, V>>[] oldTable = table;
    table = new ArrayList[2 * oldTable.length];
    size = 0; // put() to new table below will reset size back to
    // correct number

    for (int i = 0; i < oldTable.length; i++) {
      ArrayList<Entry<K, V>> bucket = oldTable[i];
      if (bucket != null) {
        bucket.forEach(entry -> {
          put(entry.key, entry.value);
          entryPool.returnObject(entry);
        });
        bucket.clear();
      }
    }
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    int bucket = getBucket((K) key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries != null) {
      for (int i = 0; i < entries.size(); i++) {
        Entry<K, V> entry = entries.get(i);
        if (Objects.equals(entry.key, key)) {
          return entry.value;
        }
      }
    }
    return null;
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public V remove(Object key) {
    int bucket = getBucket((K) key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries != null) {
      for (int i = 0; i < entries.size(); i++) {
        Entry<K, V> entry = entries.get(i);
        if (Objects.equals(entry.key, key)) {
          V oldValue = entry.value;
          entries.remove(i);
          entryPool.returnObject(entry);
          size--;
          return oldValue;
        }
      }
    }
    return null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
//      for (int j = 0; j < table.length; j++) {
//          ArrayList<Entry<K, V>> bucket = table[j];
//          if (bucket != null) {
//              for (int i = 0; i < bucket.size(); i++) {
//                  Entry<K, V> entry = bucket.get(i);
//                  if (Objects.equals(value, entry.value)) {
//                      return true;
//                  }
//              }
//          }
//      }
//    return false;
  }

  @Override
  public void clear() {
    for (int i = 0; i < table.length; i++) {
      ArrayList<Entry<K, V>> bucket = table[i];
      if (bucket != null) {
          for (int j = 0; j < bucket.size(); j++) {
              Entry<K, V> entry = bucket.get(j);
              entryPool.returnObject(entry);
          }
        bucket.clear();
      }
    }
    size = 0;
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (int j = 0; j < table.length; j++) {
      ArrayList<Entry<K, V>> bucket = table[j];
      if (bucket != null) {
        for (int i = 0; i < bucket.size(); i++) {
          Entry<K, V> entry = bucket.get(i);
          action.accept(entry.key, entry.value);
        }
      }
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException();
//    return entrySetView;
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
//      m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
  }

  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException();
  }

  private int getBucket(K key) {
    return Math.abs(key.hashCode() % table.length);
  }

  private static class Entry<K, V> /*implements Map.Entry<K, V>*/ {
    @Nullable
    K key;

    @Nullable
    V value;

//    @Override
//    public K getKey() {
//      if (key == null) {
//        throw new NullPointerException("Key should never be null");
//      }
//      return key;
//    }
//
//    @Override
//    public V getValue() {
//      if (value == null) {
//        throw new NullPointerException("Value should never be null");
//      }
//      return value;
//    }
//
//    @Override
//    public V setValue(V value) {
//      V oldValue = this.value;
//      if (oldValue == null) {
//        throw new IllegalStateException("Old value for key can never be null");
//      }
//      this.value = value;
//      return oldValue;
//    }
  }

//  /**
//   * Provides a set view on the map entries
//   *
//   * Has very limited internal use hence only required methods were implemented
//   */

  /*
  private class EntrySetView implements Set<Map.Entry<K, V>> {
    @Override
    public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
      Objects.requireNonNull(filter);
      boolean removed = false;

      for (ArrayList<Entry<K, V>> bucket : table) {
        if (bucket != null) {
          for (int i = 0; i < bucket.size(); i++) {
            Map.Entry<K, V> entry = bucket.get(i);
            if (filter.test(entry)) {
              bucket.remove(i);
              i--; // adjust index due to item removal
              entryPool.returnObject((Entry<K, V>) entry);
              size--;
              removed = true;
            }
          }
        }
      }
      return removed;
    }

    @Override
    public int size() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Map.Entry<K, V> kvEntry) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
  }
  */
}
