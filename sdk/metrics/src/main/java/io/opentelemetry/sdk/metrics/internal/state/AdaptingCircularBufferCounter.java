/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

/**
 * A circle-buffer-backed exponential counter.
 *
 * <p>The first recorded value becomes the 'baseIndex'. Going backwards leads to start/stop index
 *
 * <p>This expand start/End index as it sees values.
 *
 * <p>This class is NOT thread-safe. It is expected to be behind a synchronized incrementer.
 */
public class AdaptingCircularBufferCounter implements ExponentialCounter {
  private static final int NULL_INDEX = Integer.MIN_VALUE;
  private int endIndex = NULL_INDEX;
  private int startIndex = NULL_INDEX;
  private int baseIndex = NULL_INDEX;
  private final AdaptingIntegerArray backing;

  /** Constructs a circular buffer that will hold at most {@code maxSize} buckets. */
  public AdaptingCircularBufferCounter(int maxSize) {
    this.backing = new AdaptingIntegerArray(maxSize);
  }

  public AdaptingCircularBufferCounter(ExponentialCounter toCopy) {
    // TODO - if toCopy is an AdaptingCircularBuffer, just do a copy of the underlying array
    // and baseIndex.
    this.backing = new AdaptingIntegerArray(toCopy.getMaxSize());
    this.startIndex = NULL_INDEX;
    this.baseIndex = NULL_INDEX;
    this.endIndex = NULL_INDEX;
    for (int i = toCopy.getIndexStart(); i <= toCopy.getIndexEnd(); i++) {
      long val = toCopy.get(i);
      this.increment(i, val);
    }
  }

  @Override
  public int getIndexStart() {
    return startIndex;
  }

  @Override
  public int getIndexEnd() {
    return endIndex;
  }

  @Override
  public boolean increment(int index, long delta) {
    if (baseIndex == NULL_INDEX) {
      startIndex = index;
      endIndex = index;
      baseIndex = index;
      backing.increment(0, delta);
      return true;
    }

    if (index > endIndex) {
      // Move end, check max size
      if (index - startIndex + 1 > backing.length()) {
        return false;
      }
      endIndex = index;
    } else if (index < startIndex) {
      // Move end, check max size
      if (endIndex - index + 1 > backing.length()) {
        return false;
      }
      startIndex = index;
    }
    int realIdx = toBufferIndex(index);
    backing.increment(realIdx, delta);
    return true;
  }

  @Override
  public long get(int index) {
    return backing.get(toBufferIndex(index));
  }

  @Override
  public boolean isEmpty() {
    return baseIndex == NULL_INDEX;
  }

  @Override
  public int getMaxSize() {
    return backing.length();
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
    for (int i = startIndex; i <= endIndex; i++) {
      if (i != startIndex) {
        result.append(',');
      }
      result.append(i).append('=').append(get(i));
    }
    return result.append("}").toString();
  }
}
