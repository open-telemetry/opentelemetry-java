/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple-as-possible backing structure for exponential histogram buckets. Can be used as a baseline
 * against other data structures.
 */
public class MapCounter implements ExponentialCounter {

  private static final long NULL_INDEX = Long.MIN_VALUE;

  private final Map<Integer, AtomicLong> backing;
  private final int maxSize;
  private long indexStart;
  private long indexEnd;

  /**
   * Instantiate a MapCounter with a maximum size of maxSize.
   *
   * @param maxSize maximum window size; The max difference allowed between indexStart and indexEnd.
   */
  public MapCounter(int maxSize) {
    this.backing = new HashMap<>((int) Math.ceil(maxSize/0.75) + 1);
    this.indexEnd = NULL_INDEX;
    this.indexStart = NULL_INDEX;
    this.maxSize = maxSize;
  }

  // Constructor for copying
  public MapCounter(MapCounter mapCounter) {
    this.maxSize = mapCounter.maxSize;
    this.backing = new HashMap<>((int) Math.ceil(maxSize/0.75) + 1);
    this.indexStart = mapCounter.indexStart;
    this.indexEnd = mapCounter.indexEnd;
    mapCounter.backing.forEach((Integer i, AtomicLong v) -> this.backing.put(i, new AtomicLong(v.longValue())));
  }

  @Override
  public long getIndexStart() {
    return indexStart;
  }

  @Override
  public long getIndexEnd() {
    return indexEnd;
  }

  @Override
  public boolean increment(long index, long delta) {
    // todo verify do we actually need to restrict this?
    if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
      return false;
    }
    int i = (int) index; // safely castable due to above check
    if (indexStart == NULL_INDEX) {
      indexStart = index;
      indexEnd = index;
      doIncrement(i, delta);
      return true;
    }

    // Extend window if possible. if it would exceed maxSize, then return false.
    if (i > indexEnd) {
      if (i - indexStart + 1 > maxSize) {
        return false;
      }
      indexEnd = i;
    } else if (i < indexStart) {
      if (indexEnd - i + 1 > maxSize) {
        return false;
      }
      indexStart = i;
    }

    doIncrement(i, delta);
    return true;
  }

  @Override
  public long get(long index) {
    if (index < indexStart || index > indexEnd) {
      throw new IndexOutOfBoundsException(String.format("Index %d out of range.", index));
    }
    AtomicLong result = backing.get((int) index);
    if (result == null) {
      return 0;
    }
    return result.longValue();
  }

  @Override
  public boolean isEmpty() {
    return backing.isEmpty();
  }

  private void doIncrement(int index, long delta) {
    AtomicLong value = backing.get(index);
    if (value == null) {
      backing.put(index, new AtomicLong(delta));
    } else {
      value.getAndAdd(delta);
    }
  }
}
