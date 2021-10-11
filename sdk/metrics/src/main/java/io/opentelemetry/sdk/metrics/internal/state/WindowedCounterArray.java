/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/* Copyright 2021 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * This file is part of the NrSketch project.
 */

package io.opentelemetry.sdk.metrics.internal.state;

/**
 * A logical array with an index window. Window can start anywhere in "long" range, including
 * negative numbers.
 * Window start and end is auto updated on array write. Write will fail when window size exceeds
 * maxSize
 */
public class WindowedCounterArray {
  public static final long NULL_INDEX = Long.MIN_VALUE;

  private final MultiTypeCounterArray backingArray; // Physical storage, whose index starts from 0.
  private final int maxSize; // Maximal window size
  private long
      indexBase; // Logical index of entry 0 in backingArray. Must be within [indexStart, indexEnd]
  // TODO(jamesmoessis) make these indexes ints?
  private long indexStart; // inclusive
  private long indexEnd; // inclusive

  public WindowedCounterArray(final int maxSize) {
    this(maxSize, (byte) Byte.BYTES); // Start from smallest type
  }

  public WindowedCounterArray(final int maxSize, final byte bytesPerCounter) {
    backingArray = new MultiTypeCounterArray(maxSize, bytesPerCounter);
    this.maxSize = maxSize;
    indexBase = NULL_INDEX;
    indexStart = NULL_INDEX;
    indexEnd = NULL_INDEX;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof WindowedCounterArray)) {
      return false;
    }
    final WindowedCounterArray other = (WindowedCounterArray) obj;

    // indexBase need not be the same. We only care about logical equality.
    if (maxSize != other.maxSize || indexStart != other.indexStart || indexEnd != other.indexEnd) {
      return false;
    }

    if (indexStart != NULL_INDEX) {
      for (long index = indexStart; index <= indexEnd; index++) {
        if (get(index) != other.get(index)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() { // Defined just to keep findbugs happy.
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("maxSize=" + maxSize);
    builder.append(", indexBase=" + indexBase);
    builder.append(", indexStart=" + indexStart);
    builder.append(", indexEnd=" + indexEnd);

    if (indexStart != NULL_INDEX) {
      builder.append(", array={");
      for (long index = indexStart; index <= indexEnd; index++) {
        builder.append(get(index));
        builder.append(",");
      }
      builder.append("}");
    }
    return builder.toString();
  }

  public int getMaxSize() {
    return maxSize;
  }

  public long getWindowSize() {
    return indexBase == NULL_INDEX ? 0 : indexEnd - indexStart + 1;
  }

  public byte getBytesPerCounter() {
    return backingArray.getBytesPerCounter();
  }

  public boolean isEmpty() {
    return getWindowSize() <= 0;
  }

  public long getIndexStart() {
    return indexStart;
  }

  public long getIndexEnd() {
    return indexEnd;
  }

  // Returns true for success. False for failure (cannot fit index into window)
  public boolean increment(final long index, final long delta) {
    if (indexBase == NULL_INDEX) {
      indexBase = index;
      indexStart = index;
      indexEnd = index;
      backingArray.increment(0, delta);
      return true;
    }
    if (index > indexEnd) {
      if (index - indexStart + 1 > maxSize) {
        return false; // Size overflow
      }
      indexEnd = index; // Extend window end

    } else if (index < indexStart) {
      if (indexEnd - index + 1 > maxSize) {
        return false; // Size overflow
      }
      indexStart = index; // Extend window start
    }
    backingArray.increment(getBackingArrayIndex(index), delta);
    return true;
  }

  public long get(final long index) {
    if (index < indexStart || index > indexEnd) {
      throw new IndexOutOfBoundsException(
          "index=" + index + " indexStart=" + indexStart + " indexEnd=" + indexEnd);
    }
    return backingArray.get(getBackingArrayIndex(index));
  }

  // Handle index wrap-around in backing array.
  private int getBackingArrayIndex(final long index) {
    int backArrayIndex = (int) (index - indexBase);
    if (backArrayIndex >= maxSize) {
      backArrayIndex -= maxSize;
    } else if (backArrayIndex < 0) {
      backArrayIndex += maxSize;
    }
    return backArrayIndex;
  }
}
