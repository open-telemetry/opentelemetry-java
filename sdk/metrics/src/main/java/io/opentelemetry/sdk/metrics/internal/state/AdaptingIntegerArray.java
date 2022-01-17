/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

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
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class AdaptingIntegerArray {
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
  public AdaptingIntegerArray(int size) {
    this.cellSize = ArrayCellSize.BYTE;
    this.byteBacking = new byte[size];
  }

  private AdaptingIntegerArray(int size, ArrayCellSize cellSize) {
    this.cellSize = cellSize;
    switch (cellSize) {
      case BYTE:
        this.byteBacking = new byte[size];
        break;
      case SHORT:
        this.shortBacking = new short[size];
        break;
      case INT:
        this.intBacking = new int[size];
        break;
      case LONG:
        this.longBacking = new long[size];
        break;
    }
  }

  /** Creates deep copy of another adapting integer array. */
  @SuppressWarnings("NullAway")
  public AdaptingIntegerArray(AdaptingIntegerArray toCopy) {
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

  /**
   * Copies the size + integer cell size of another array, but allocating a new array of zero
   * counts.
   *
   * @param toZero The other addapting integer array to mirror the size of.
   */
  public static AdaptingIntegerArray zeroOf(AdaptingIntegerArray toZero) {
    return new AdaptingIntegerArray(toZero.length(), toZero.cellSize);
  }

  /** Add {@code count} to the value stored at {@code index}. */
  @SuppressWarnings("NullAway")
  public void increment(int idx, long count) {
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
  public long get(int index) {
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
  public int length() {
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

  /** Convert from byte => short backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToShort() {
    this.shortBacking = new short[this.byteBacking.length];
    for (int i = 0; i < this.byteBacking.length; i++) {
      this.shortBacking[i] = this.byteBacking[i];
    }
    this.cellSize = ArrayCellSize.SHORT;
    this.byteBacking = null;
  }

  /** Convert from short => int backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToInt() {
    this.intBacking = new int[this.shortBacking.length];
    for (int i = 0; i < this.shortBacking.length; i++) {
      this.intBacking[i] = this.shortBacking[i];
    }
    this.cellSize = ArrayCellSize.INT;
    this.shortBacking = null;
  }
  /** convert from int => long backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToLong() {
    this.longBacking = new long[this.intBacking.length];
    for (int i = 0; i < this.intBacking.length; i++) {
      this.longBacking[i] = this.intBacking[i];
    }
    this.cellSize = ArrayCellSize.LONG;
    this.intBacking = null;
  }
}
