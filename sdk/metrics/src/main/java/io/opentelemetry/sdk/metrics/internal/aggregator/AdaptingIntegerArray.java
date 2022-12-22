/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * An integer array that automatically expands its memory consumption (via copy/allocation) when
 * reaching limits. This assumes counts remain low, to lower memory overhead.
 *
 * <p>This class is NOT thread-safe. It is expected to be behind a synchronized incrementer.
 *
 * <p>Instances start by attempting to store one-byte per-cell in the integer array. As values grow,
 * this will automatically instantiate the next-size integer array (byte => short => int => long)
 * and copy over values into the larger array. This class expects most usage to remain within the
 * byte boundary (e.g. cell values < 128).
 *
 * <p>This class lives in the (very) hot path of metric recording. As such, we do "fun" things, like
 * switch on markers and assume non-null based on presence of the markers, as such we suppress
 * NullAway as it can't understand/express this level of guarantee.
 *
 * <p>Implementations MUST preserve the following:
 *
 * <ul>
 *   <li>If cellSize == BYTE then byteBacking is not null
 *   <li>If cellSize == SHORT then shortBacking is not null
 *   <li>If cellSize == INT then intBacking is not null
 *   <li>If cellSize == LONG then longBacking is not null
 * </ul>
 */
final class AdaptingIntegerArray {
  @Nullable private byte[] byteBacking;
  @Nullable private short[] shortBacking;
  @Nullable private int[] intBacking;
  @Nullable private long[] longBacking;

  /** Possible sizes of backing arrays. */
  private enum ArrayCellSize {
    BYTE,
    SHORT,
    INT,
    LONG
  }
  /** The current byte size of integer cells in this array. */
  private ArrayCellSize cellSize;

  /** Construct an adapting integer array of a given size. */
  AdaptingIntegerArray(int size) {
    this.cellSize = ArrayCellSize.BYTE;
    this.byteBacking = new byte[size];
  }

  /** Creates deep copy of another adapting integer array. */
  @SuppressWarnings("NullAway")
  private AdaptingIntegerArray(AdaptingIntegerArray toCopy) {
    this.cellSize = toCopy.cellSize;
    switch (cellSize) {
      case BYTE:
        this.byteBacking = Arrays.copyOf(toCopy.byteBacking, toCopy.byteBacking.length);
        break;
      case SHORT:
        this.shortBacking = Arrays.copyOf(toCopy.shortBacking, toCopy.shortBacking.length);
        break;
      case INT:
        this.intBacking = Arrays.copyOf(toCopy.intBacking, toCopy.intBacking.length);
        break;
      case LONG:
        this.longBacking = Arrays.copyOf(toCopy.longBacking, toCopy.longBacking.length);
        break;
    }
  }

  /** Returns a deep-copy of this array, preserving cell size. */
  AdaptingIntegerArray copy() {
    return new AdaptingIntegerArray(this);
  }

  /** Add {@code count} to the value stored at {@code index}. */
  @SuppressWarnings("NullAway")
  void increment(int idx, long count) {
    // TODO - prevent bad index
    long result;
    switch (cellSize) {
      case BYTE:
        result = byteBacking[idx] + count;
        if (result > Byte.MAX_VALUE) {
          // Resize + add
          resizeToShort();
          increment(idx, count);
          return;
        }
        byteBacking[idx] = (byte) result;
        return;
      case SHORT:
        result = shortBacking[idx] + count;
        if (result > Short.MAX_VALUE) {
          resizeToInt();
          increment(idx, count);
          return;
        }
        shortBacking[idx] = (short) result;
        return;
      case INT:
        result = intBacking[idx] + count;
        if (result > Integer.MAX_VALUE) {
          resizeToLong();
          increment(idx, count);
          return;
        }
        intBacking[idx] = (int) result;
        return;
      case LONG:
        longBacking[idx] = longBacking[idx] + count;
        return;
    }
  }

  /** Grab the value stored at {@code index}. */
  @SuppressWarnings("NullAway")
  long get(int index) {
    long value = 0;
    switch (this.cellSize) {
      case BYTE:
        value = this.byteBacking[index];
        break;
      case SHORT:
        value = this.shortBacking[index];
        break;
      case INT:
        value = this.intBacking[index];
        break;
      case LONG:
        value = this.longBacking[index];
        break;
    }
    return value;
  }

  /** Return the length of this integer array. */
  @SuppressWarnings("NullAway")
  int length() {
    int length = 0;
    switch (this.cellSize) {
      case BYTE:
        length = this.byteBacking.length;
        break;
      case SHORT:
        length = this.shortBacking.length;
        break;
      case INT:
        length = this.intBacking.length;
        break;
      case LONG:
        length = this.longBacking.length;
        break;
    }
    return length;
  }
  /** Zeroes out all counts in this array. */
  @SuppressWarnings("NullAway")
  void clear() {
    switch (this.cellSize) {
      case BYTE:
        Arrays.fill(this.byteBacking, (byte) 0);
        break;
      case SHORT:
        Arrays.fill(this.shortBacking, (short) 0);
        break;
      case INT:
        Arrays.fill(this.intBacking, 0);
        break;
      case LONG:
        Arrays.fill(this.longBacking, 0L);
        break;
    }
  }

  /** Convert from byte => short backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToShort() {
    short[] shortBacking = new short[this.byteBacking.length];
    for (int i = 0; i < this.byteBacking.length; i++) {
      shortBacking[i] = this.byteBacking[i];
    }
    this.cellSize = ArrayCellSize.SHORT;
    this.shortBacking = shortBacking;
    this.byteBacking = null;
  }

  /** Convert from short => int backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToInt() {
    int[] intBacking = new int[this.shortBacking.length];
    for (int i = 0; i < this.shortBacking.length; i++) {
      intBacking[i] = this.shortBacking[i];
    }
    this.cellSize = ArrayCellSize.INT;
    this.intBacking = intBacking;
    this.shortBacking = null;
  }
  /** convert from int => long backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToLong() {
    long[] longBacking = new long[this.intBacking.length];
    for (int i = 0; i < this.intBacking.length; i++) {
      longBacking[i] = this.intBacking[i];
    }
    this.cellSize = ArrayCellSize.LONG;
    this.longBacking = longBacking;
    this.intBacking = null;
  }
}
