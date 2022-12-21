/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

/**
 * A circle-buffer-backed exponential counter.
 *
 * <p>The first recorded value becomes the 'baseIndex'. Going backwards leads to start/stop index
 *
 * <p>This expand start/End index as it sees values.
 *
 * <p>This class is NOT thread-safe. It is expected to be behind a synchronized incrementer.
 */
final class AdaptingCircularBufferCounter {
  private static final int NULL_INDEX = Integer.MIN_VALUE;
  private int endIndex = NULL_INDEX;
  private int startIndex = NULL_INDEX;
  private int baseIndex = NULL_INDEX;
  private final AdaptingIntegerArray backing;

  /** Constructs a circular buffer that will hold at most {@code maxSize} buckets. */
  AdaptingCircularBufferCounter(int maxSize) {
    this.backing = new AdaptingIntegerArray(maxSize);
  }

  /** (Deep)-Copies the values from another exponential counter. */
  AdaptingCircularBufferCounter(AdaptingCircularBufferCounter toCopy) {
    this.backing = toCopy.backing.copy();
    this.startIndex = toCopy.getIndexStart();
    this.endIndex = toCopy.getIndexEnd();
    this.baseIndex = toCopy.baseIndex;
  }

  /**
   * The first index with a recording. May be negative.
   *
   * <p>Note: the returned value is not meaningful when isEmpty returns true.
   *
   * @return the first index with a recording.
   */
  int getIndexStart() {
    return startIndex;
  }

  /**
   * The last index with a recording. May be negative.
   *
   * <p>Note: the returned value is not meaningful when isEmpty returns true.
   *
   * @return The last index with a recording.
   */
  int getIndexEnd() {
    return endIndex;
  }

  /**
   * Persist new data at index, incrementing by delta amount.
   *
   * @param index The index of where to perform the incrementation.
   * @param delta How much to increment the index by.
   * @return success status.
   */
  boolean increment(int index, long delta) {
    if (baseIndex == NULL_INDEX) {
      startIndex = index;
      endIndex = index;
      baseIndex = index;
      backing.increment(0, delta);
      return true;
    }

    if (index > endIndex) {
      // Move end, check max size
      if ((long) index - startIndex + 1 > backing.length()) {
        return false;
      }
      endIndex = index;
    } else if (index < startIndex) {
      // Move end, check max size
      if ((long) endIndex - index + 1 > backing.length()) {
        return false;
      }
      startIndex = index;
    }
    int realIdx = toBufferIndex(index);
    backing.increment(realIdx, delta);
    return true;
  }

  /**
   * Get the number of recordings for the given index.
   *
   * @return the number of recordings for the index, or 0 if the index is out of bounds.
   */
  long get(int index) {
    if (index < startIndex || index > endIndex) {
      return 0;
    }
    return backing.get(toBufferIndex(index));
  }

  /**
   * Boolean denoting if the backing structure has recordings or not.
   *
   * @return true if no recordings, false if at least one recording.
   */
  boolean isEmpty() {
    return baseIndex == NULL_INDEX;
  }

  /** Returns the maximum number of buckets allowed in this counter. */
  int getMaxSize() {
    return backing.length();
  }

  /** Resets all bucket counts to zero and resets index start/end tracking. */
  void clear() {
    this.backing.clear();
    this.baseIndex = NULL_INDEX;
    this.endIndex = NULL_INDEX;
    this.startIndex = NULL_INDEX;
  }

  private int toBufferIndex(int index) {
    // Figure out the index relative to the start of the circular buffer.
    int result = index - baseIndex;
    if (result >= backing.length()) {
      result -= backing.length();
    } else if (result < 0) {
      result += backing.length();
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("{");
    for (int i = startIndex; i <= endIndex && startIndex != NULL_INDEX; i++) {
      if (i != startIndex) {
        result.append(',');
      }
      result.append(i).append('=').append(get(i));
    }
    return result.append("}").toString();
  }
}
