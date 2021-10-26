/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

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

  public static final int MAX_SIZE = 320;

  private static final long NULL_INDEX = Long.MIN_VALUE;

  private final ConcurrentHashMap<Integer, AtomicLong> backing;
  private long indexStart;
  private long indexEnd;

  /** Instantiate a MapCounter. */
  public MapCounter() {
    this.backing = new ConcurrentHashMap<>((int) Math.ceil(MAX_SIZE / 0.75) + 1);
    this.indexEnd = NULL_INDEX;
    this.indexStart = NULL_INDEX;
  }

  /**
   * Create an independent copy of another ExponentialCounter.
   *
   * @param otherCounter another exponential counter to make a deep copy of.
   */
  public MapCounter(ExponentialCounter otherCounter) {
    this.backing = new ConcurrentHashMap<>((int) Math.ceil(MAX_SIZE / 0.75) + 1);
    this.indexStart = otherCounter.getIndexStart();
    this.indexEnd = otherCounter.getIndexEnd();

    // copy values
    for (long i = indexStart; i <= indexEnd; i++) {
      long val = otherCounter.get(i);
      if (val != 0) {
        this.backing.put((int) i, new AtomicLong(val));
      }
    }
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
    int i = (int) index;
    if (indexStart == NULL_INDEX) {
      indexStart = index;
      indexEnd = index;
      doIncrement(i, delta);
      return true;
    }

    // Extend window if possible. if it would exceed maxSize, then return false.
    if (i > indexEnd) {
      if (i - indexStart + 1 > MAX_SIZE) {
        return false;
      }
      indexEnd = i;
    } else if (i < indexStart) {
      if (indexEnd - i + 1 > MAX_SIZE) {
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
