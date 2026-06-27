/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/**
 * A map with a fixed capacity that drops attributes when the map gets full, and which truncates
 * string and array string attribute values to the {@link #lengthLimit}.
 *
 * <p>Keyed internally by attribute name, so that attributes with the same name but different types
 * are treated as the same key (last-value-wins), consistent with the OpenTelemetry specification.
 *
 * <p>Backed by parallel arrays and an open-addressing {@code int[]} hash table (linear probing,
 * load factor ≤ 0.5). Avoids per-entry object allocation; {@code forEach} is a tight sequential
 * array loop with no pointer chasing.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributesMap implements Attributes {

  /**
   * Sentinel value meaning "slot is empty" in the hash table. Using 0 lets {@code new int[n]} (JVM
   * zero-initialization) serve as the initial fill, eliminating explicit {@code Arrays.fill} calls.
   * Occupied slots store {@code entryIndex + 1} so that index 0 is distinguishable from EMPTY.
   */
  private static final int EMPTY = 0;

  private final int capacity;
  private final int lengthLimit;
  private int totalAddedValues = 0;
  private int size = 0;

  /**
   * Open-addressing hash table: {@code hashTable[slot]} = index into entry arrays, or {@link
   * #EMPTY}. Length is always a power of 2 and ≥ 2× the entry array length (load factor ≤ 0.5).
   */
  private int[] hashTable;

  /** Cached {@code hashTable.length - 1}; kept in sync with {@link #hashTable}. */
  private int mask;

  /** Parallel entry arrays. Index {@code i} holds the i-th inserted entry. */
  private String[] entryNames;

  private AttributeKey<?>[] entryKeys;
  private Object[] entryValues;

  private AttributesMap(long capacity, int lengthLimit) {
    this.capacity = (int) Math.min(capacity, Integer.MAX_VALUE);
    this.lengthLimit = lengthLimit;
    int init = (int) Math.min(capacity, 16L);
    entryNames = new String[init];
    entryKeys = new AttributeKey<?>[init];
    entryValues = new Object[init];
    hashTable = new int[tableSizeFor(init)]; // JVM zero-init == EMPTY
    mask = hashTable.length - 1;
  }

  /**
   * Create an instance.
   *
   * @param capacity the max number of attribute entries
   * @param lengthLimit the maximum length of string attributes
   */
  public static AttributesMap create(long capacity, int lengthLimit) {
    return new AttributesMap(capacity, lengthLimit);
  }

  /**
   * Add the attribute key value pair, applying capacity and length limits. Callers MUST ensure the
   * {@code value} type matches the type required by {@code key}.
   *
   * <p>If an attribute with the same string key name already exists (regardless of type), it is
   * overwritten — last-value-wins, consistent with the OTel spec.
   */
  @Nullable
  public Object put(AttributeKey<?> key, @Nullable Object value) {
    if (value == null) {
      return null;
    }
    totalAddedValues++;
    String name = key.getKey();
    int slot = findSlot(name);
    int stored = hashTable[slot]; // EMPTY or 1-based index
    if (stored == EMPTY) {
      if (size >= capacity) {
        return null;
      }
      Object limited = AttributeUtil.applyAttributeLengthLimit(value, lengthLimit);
      if (size == entryNames.length) {
        grow();
        slot = findSlot(name); // hashTable was rebuilt by grow()
      }
      entryNames[size] = name;
      entryKeys[size] = key;
      entryValues[size] = limited;
      hashTable[slot] = size + 1; // 1-based
      size++;
      return null;
    }
    int idx = stored - 1;
    Object old = entryValues[idx];
    entryKeys[idx] = key;
    entryValues[idx] = AttributeUtil.applyAttributeLengthLimit(value, lengthLimit);
    return old;
  }

  /** Generic overload of {@link #put(AttributeKey, Object)}. */
  public <T> void putIfCapacity(AttributeKey<T> key, @Nullable T value) {
    put(key, value);
  }

  /** Get the total number of attributes added, including those dropped for capacity limits. */
  public int getTotalAddedValues() {
    return totalAddedValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nullable
  public <T> T get(AttributeKey<T> key) {
    int stored = hashTable[findSlot(key.getKey())];
    if (stored == EMPTY) {
      return null;
    }
    int idx = stored - 1;
    if (!entryKeys[idx].getType().equals(key.getType())) {
      return null;
    }
    return (T) entryValues[idx];
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
  public Map<AttributeKey<?>, Object> asMap() {
    Map<AttributeKey<?>, Object> snapshot = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      snapshot.put(entryKeys[i], entryValues[i]);
    }
    return Collections.unmodifiableMap(snapshot);
  }

  @Override
  public AttributesBuilder toBuilder() {
    return Attributes.builder().putAll(this);
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> action) {
    for (int i = 0; i < size; i++) {
      action.accept(entryKeys[i], entryValues[i]);
    }
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attributes)) {
      return false;
    }
    return asMap().equals(((Attributes) o).asMap());
  }

  @Override
  public int hashCode() {
    return asMap().hashCode();
  }

  @Override
  public String toString() {
    return "AttributesMap{"
        + "data="
        + asMap()
        + ", capacity="
        + capacity
        + ", totalAddedValues="
        + totalAddedValues
        + '}';
  }

  /** Create an immutable copy of the attributes in this map. */
  public Attributes immutableCopy() {
    return Attributes.builder().putAll(this).build();
  }

  /**
   * Returns the hash table slot that either contains the entry for {@code name} or is the first
   * empty slot available for insertion. Single shared probe loop used by {@code put}, {@code get},
   * and {@code grow}. Slots store {@code entryIndex + 1}; 0 ({@link #EMPTY}) means unoccupied.
   */
  private int findSlot(String name) {
    int slot = name.hashCode() & mask;
    int stored;
    while ((stored = hashTable[slot]) != EMPTY && !entryNames[stored - 1].equals(name)) {
      slot = (slot + 1) & mask;
    }
    return slot;
  }

  private void grow() {
    long maxLen = Math.min(capacity, (long) Integer.MAX_VALUE - 8);
    int newLen = (int) Math.min((long) entryNames.length * 2, maxLen);
    entryNames = Arrays.copyOf(entryNames, newLen);
    entryKeys = Arrays.copyOf(entryKeys, newLen);
    entryValues = Arrays.copyOf(entryValues, newLen);
    hashTable = new int[tableSizeFor(newLen)]; // JVM zero-init == EMPTY
    mask = hashTable.length - 1;
    for (int i = 0; i < size; i++) {
      int slot = findSlot(entryNames[i]);
      hashTable[slot] = i + 1; // 1-based
    }
  }

  /**
   * Returns the smallest power of 2 that is ≥ 2n, guaranteeing load factor ≤ 0.5. Using {@code
   * (2n-1)} instead of {@code 2n} prevents doubling the result when {@code n} is itself a power of
   * 2.
   */
  private static int tableSizeFor(int n) {
    if (n <= 2) {
      return 4;
    }
    return Integer.highestOneBit(2 * n - 1) << 1;
  }
}
