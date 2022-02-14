/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple-as-possible backing structure for exponential histogram buckets. Can be used as a baseline
 * against other data structures.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time
 */
public class MapCounter implements ExponentialCounter {
  private static final int NULL_INDEX = Integer.MIN_VALUE;

  private final int maxSize;
  private final Map<Integer, AtomicLong> backing;
  private int indexStart;
  private int indexEnd;

  /** Instantiate a MapCounter. */
  public MapCounter(int maxSize) {
    this.maxSize = maxSize;
    this.backing = new ConcurrentHashMap<>((int) Math.ceil(maxSize / 0.75) + 1);
    this.indexEnd = NULL_INDEX;
    this.indexStart = NULL_INDEX;
  }

  /**
   * Create an independent copy of another ExponentialCounter.
   *
   * @param otherCounter another exponential counter to make a deep copy of.
   */
  public MapCounter(ExponentialCounter otherCounter) {
    this.maxSize = otherCounter.getMaxSize();
    this.backing = new ConcurrentHashMap<>((int) Math.ceil(maxSize / 0.75) + 1);
    this.indexStart = otherCounter.getIndexStart();
    this.indexEnd = otherCounter.getIndexEnd();

    // copy values
    for (int i = indexStart; i <= indexEnd; i++) {
      long val = otherCounter.get(i);
      if (val != 0) {
        this.backing.put(i, new AtomicLong(val));
      }
    }
  }

  @Override
  public int getIndexStart() {
    return indexStart;
  }

  @Override
  public int getIndexEnd() {
    return indexEnd;
  }

  @Override
  public boolean increment(int index, long delta) {
    if (indexStart == NULL_INDEX) {
      indexStart = index;
      indexEnd = index;
      doIncrement(index, delta);
      return true;
    }

    // Extend window if possible. if it would exceed maxSize, then return false.
    if (index > indexEnd) {
      if ((long) index - indexStart + 1 > maxSize) {
        return false;
      }
      indexEnd = index;
    } else if (index < indexStart) {
      if ((long) indexEnd - index + 1 > maxSize) {
        return false;
      }
      indexStart = index;
    }

    doIncrement(index, delta);
    return true;
  }

  @Override
  public long get(int index) {
    if (index < indexStart || index > indexEnd) {
      return 0;
    }
    AtomicLong result = backing.get(index);
    if (result == null) {
      return 0;
    }
    return result.longValue();
  }

  @Override
  public boolean isEmpty() {
    return backing.isEmpty();
  }

  @Override
  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public void clear() {
    this.backing.clear();
    this.indexStart = NULL_INDEX;
    this.indexEnd = NULL_INDEX;
  }

  private synchronized void doIncrement(int index, long delta) {
    long prevValue = backing.computeIfAbsent(index, k -> new AtomicLong(0)).getAndAdd(delta);

    // in the case of a decrement result may be 0, so we remove the entry
    if (prevValue + delta == 0) {
      backing.remove(index);
      if (isEmpty()) {
        indexStart = NULL_INDEX;
        indexEnd = NULL_INDEX;
      } else {
        // find largest and smallest index to remap window
        indexStart = Collections.min(backing.keySet());
        indexEnd = Collections.max(backing.keySet());
      }
    }
  }

  @Override
  public String toString() {
    return backing.toString();
  }
}
