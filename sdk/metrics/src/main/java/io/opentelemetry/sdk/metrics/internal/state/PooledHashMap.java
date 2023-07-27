package io.opentelemetry.sdk.metrics.internal.state;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class PooledHashMap<K, V> implements Map<K, V> {
  private static final int DEFAULT_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;

  private ArrayList<Entry<K, V>>[] table;
  private final ObjectPool<Entry<K, V>> entryPool;
  private int size;
  private final EntrySetView entrySetView;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public PooledHashMap(int capacity) {
    this.table = new ArrayList[capacity];
    this.entryPool = new ObjectPool<>(Entry::new);
    this.size = 0;
    entrySetView = new EntrySetView();
  }

  public PooledHashMap() {
    this(DEFAULT_CAPACITY);
  }

  @Override
  @Nullable
  public V put(K key, V value) {
    return putInternal(key, value);
  }

  @Nullable
  private V putInternal(@Nullable K key, @Nullable V value) {
    if (size > LOAD_FACTOR * table.length) {
      rehash();
    }
    if (key == null) {
      throw new NullPointerException();
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


  @SuppressWarnings({"rawtypes", "unchecked"})
  private void rehash() {
    ArrayList<Entry<K, V>>[] oldTable = table;
    table = new ArrayList[2 * oldTable.length];
    size = 0;

    for (ArrayList<Entry<K, V>> bucket : oldTable) {
      if (bucket != null) {
        bucket.forEach(entry -> {
          if (entry.key == null) {
            throw new NullPointerException();
          }
          if (entry.value == null) {
            throw new NullPointerException();
          }
          put(entry.key, entry.value);
          entryPool.returnObject(entry);
        });
        bucket.clear();
      }
    }
  }

  @Override
  @Nullable
  @SuppressWarnings({"unchecked"})
  public V get(Object key) {
    int bucket = getBucket((K) key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries != null) {
      for (Entry<K, V> entry : entries) {
        if (Objects.equals(entry.key, key)) {
          return entry.value;
        }
      }
    }
    return null;
  }

  public void fillWithValues(ArrayList<V> list) {
    list.clear();
    for (ArrayList<Entry<K, V>> bucket : table) {
      if (bucket != null) {
        bucket.forEach(entry -> list.add(entry.value));
      }
    }
  }

  @Override
  @Nullable
  @SuppressWarnings({"unchecked"})
  public V remove(Object key) {
    int bucket = getBucket((K) key);
    ArrayList<Entry<K, V>> entries = table[bucket];
    if (entries != null) {
      Iterator<Entry<K, V>> iterator = entries.iterator();
      while (iterator.hasNext()) {
        Entry<K, V> entry = iterator.next();
        if (Objects.equals(entry.key, key)) {
          V oldValue = entry.value;
          iterator.remove();
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
    for (ArrayList<Entry<K, V>> bucket : table) {
      if (bucket != null) {
        for (Entry<K, V> entry : bucket) {
          if (Objects.equals(value, entry.value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public void clear() {
    for (ArrayList<Entry<K, V>> bucket : table) {
      if (bucket != null) {
        bucket.forEach(entryPool::returnObject);
        bucket.clear();
      }
    }
    size = 0;
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (ArrayList<Entry<K, V>> bucket : table) {
      if (bucket != null) {
        for (int i = 0; i < bucket.size(); i++) {
          Entry<K, V> entry = bucket.get(i);
          action.accept(entry.key, entry.value);
        }
      }
    }
  }

  // These methods are part of the Map interface but are not implemented.
  // They will throw UnsupportedOperationException until implemented.

  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return entrySetView;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  // The rest of the unimplemented methods from the Map interface

  private int getBucket(K key) {
    return Math.abs(key.hashCode() % table.length);
  }

  private static class Entry<K, V> implements Map.Entry<K, V>{
    @Nullable
    K key;
    @Nullable
    V value;

    @Override
    public K getKey() {
      if (key == null) {
        throw new NullPointerException();
      }
      return key;
    }

    @Override
    public V getValue() {
      if (value == null) {
        throw new NullPointerException();
      }
      return value;
    }

    @Override
    public V setValue(V value) {
      if (this.value == null) {
        throw new NullPointerException();
      }
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }
  }

  public class EntrySetView implements Set<Map.Entry<K, V>> {
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

}
