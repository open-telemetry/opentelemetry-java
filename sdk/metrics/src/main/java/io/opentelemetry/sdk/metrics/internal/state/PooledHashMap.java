/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/**
 * A bucket-based hash map with an internal re-usable map entry objects pool.
 *
 * <p>The goal of this map is to minimize memory allocation, leading to reduced time spent in
 * garbage collection.
 *
 * <p>This map avoids allocating a new map entry on each put operation by maintaining a pool of
 * reusable (mutable) map entries and borrowing a map entry object from the pool to hold the given
 * key-value of the put operation. The borrowed object is returned to the pool when the map entry
 * key is removed from the map.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 *
 * @param <K> The map key type
 * @param <V> The map value type
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class PooledHashMap<K, V> implements Map<K, V> {
  private static final int DEFAULT_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;

  private ArrayList<Entry<K, V>>[] table;
  private final ObjectPool<Entry<K, V>> entryPool;
  private int size;

  /**
   * Creates a {@link PooledHashMap} with {@code capacity} buckets.
   *
   * <p>The hashmap contains an array of buckets, each is an array-list of items. The number of
   * buckets expands over time to avoid having too many items in one bucket, otherwise accessing an
   * item by key won't be a constant time complexity.
   *
   * @param capacity The initial number of buckets to start with
   */
  @SuppressWarnings({"unchecked"})
  public PooledHashMap(int capacity) {
    this.table = (ArrayList<Entry<K, V>>[]) new ArrayList<?>[capacity];
    this.entryPool = new ObjectPool<>(Entry::new);
    this.size = 0;
  }

  /**
   * Creates a new {@link PooledHashMap} with a default amount of buckets (capacity).
   *
   * @see PooledHashMap#PooledHashMap(int)
   */
  public PooledHashMap() {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Add a key, value pair to the map.
   *
   * <p>Internally it uses a MapEntry from a pool of entries, to store this mapping
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return Null if the was no previous mapping for this key, or the value of the previous mapping
   *     of this key
   */
  @Override
  @Nullable
  public V put(K key, V value) {
    requireNonNull(key, "This map does not support null keys");
    requireNonNull(value, "This map does not support null values");
    if (size > LOAD_FACTOR * table.length) {
      rehash();
    }

    int bucket = getBucket(key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries == null) {
      entries = new ArrayList<>();
      table[bucket] = entries;
    } else {
      // Don't optimize to enhanced for-loop since implicit iterator used allocated memory in O(n)
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

  @SuppressWarnings({"unchecked"})
  private void rehash() {
    ArrayList<Entry<K, V>>[] oldTable = table;
    table = (ArrayList<Entry<K, V>>[]) new ArrayList<?>[2 * oldTable.length];

    // put() to new table below will reset size back to correct number
    size = 0;

    for (int i = 0; i < oldTable.length; i++) {
      ArrayList<Entry<K, V>> bucket = oldTable[i];
      if (bucket != null) {
        for (Entry<K, V> entry : bucket) {
          put(requireNonNull(entry.key), requireNonNull(entry.value));
          entryPool.returnObject(entry);
        }
        bucket.clear();
      }
    }
  }

  /**
   * Retrieves the mapped value for {@code key}.
   *
   * @param key the key whose associated value is to be returned
   * @return The mapped value for {@code key} or null if there is no such mapping
   */
  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    requireNonNull(key, "This map does not support null keys");

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

  /**
   * Removes the mapping for the given {@code key}.
   *
   * @param key key whose mapping is to be removed from the map
   * @return The value mapped to this key, if the mapping exists, or null otherwise
   */
  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public V remove(Object key) {
    requireNonNull(key, "This map does not support null keys");

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
    requireNonNull(key, "This map does not support null keys");

    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
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

  private int getBucket(K key) {
    return Math.abs(key.hashCode() % table.length);
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException();
  }

  private static class Entry<K, V> {
    @Nullable K key;

    @Nullable V value;
  }
}
