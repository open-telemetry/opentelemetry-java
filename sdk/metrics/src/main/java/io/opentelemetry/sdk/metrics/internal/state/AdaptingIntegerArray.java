/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import javax.annotation.Nullable;

/**
 * An integer array that automatically expands its memory consumption (via copy/allocation) when
 * reaching limits. This assumes counts remain low, to lower memory overhead.
 *
 * <p>This class lives in the (very) hot path of metric recording. As such, we do "fun" things, like
 * switch on markers and assume non-null based on presence of the markers, as such we suppress
 * NullAway as it can't understand/express this level of guarantee.
 *
 * <p>Implementations MUST preserve the following:
 *
 * <ul>
 *   <li>If cellSizeBytes == Byte.BYTES then byteBacking is not null
 *   <li>If cellSizeBytes == Short.BYTES then shortBacking is not null
 *   <li>If cellSizeBytes == Integer.BYTES then intBacking is not null
 *   <li>If cellSizeBytes == Long.BYTES then longBacking is not null
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
  /** The current byte size of integer cells in this array. */
  private byte cellSizeBytes;

  /** Construct an adapting integer array of a given size. */
  public AdaptingIntegerArray(int size) {
    this(size, Byte.BYTES);
  }

  /** Construct an adapting integer array with given size and integer size in bytes. */
  public AdaptingIntegerArray(int size, int cellSize) {
    this.cellSizeBytes = (byte) cellSize;
    switch (cellSize) {
      case Byte.BYTES:
        this.byteBacking = new byte[size];
        break;
      case Short.BYTES:
        this.shortBacking = new short[size];
        break;
      case Integer.BYTES:
        this.intBacking = new int[size];
        break;
      case Long.BYTES:
        this.longBacking = new long[size];
        break;
      default:
        throw new IllegalStateException("Unexpected integer size: " + cellSizeBytes);
    }
  }

  @SuppressWarnings("NullAway")
  public void increment(int idx, long count) {
    // TODO - prevent bad index
    long result;
    switch (cellSizeBytes) {
      case Byte.BYTES:
        result = byteBacking[idx] + count;
        if (result > Byte.MAX_VALUE) {
          // Resize + add
          resizeToShort();
          increment(idx, count);
          return;
        }
        byteBacking[idx] = (byte) result;
        return;
      case Short.BYTES:
        result = shortBacking[idx] + count;
        if (result > Short.MAX_VALUE) {
          resizeToInt();
          increment(idx, count);
          return;
        }
        shortBacking[idx] = (short) result;
        return;
      case Integer.BYTES:
        result = intBacking[idx] + count;
        if (result > Integer.MAX_VALUE) {
          resizeToLong();
          increment(idx, count);
          return;
        }
        intBacking[idx] = (int) result;
        return;
      case Long.BYTES:
        longBacking[idx] = longBacking[idx] + count;
        return;
      default:
        throw new IllegalStateException("Unexpected integer size: " + this.cellSizeBytes);
    }
  }

  @SuppressWarnings("NullAway")
  public long get(int index) {
    switch (this.cellSizeBytes) {
      case Byte.BYTES:
        return this.byteBacking[index];
      case Short.BYTES:
        return this.shortBacking[index];
      case Integer.BYTES:
        return this.intBacking[index];
      case Long.BYTES:
        return this.longBacking[index];
      default:
        throw new IllegalStateException("Unexpected integer size: " + this.cellSizeBytes);
    }
  }

  @SuppressWarnings("NullAway")
  public int length() {
    switch (this.cellSizeBytes) {
      case Byte.BYTES:
        return this.byteBacking.length;
      case Short.BYTES:
        return this.shortBacking.length;
      case Integer.BYTES:
        return this.intBacking.length;
      case Long.BYTES:
        return this.longBacking.length;
      default:
        throw new IllegalStateException("Unexpected integer size: " + this.cellSizeBytes);
    }
  }

  /** Convert from byte => short backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToShort() {
    this.shortBacking = new short[this.byteBacking.length];
    for (int i = 0; i < this.byteBacking.length; i++) {
      this.shortBacking[i] = this.byteBacking[i];
    }
    this.cellSizeBytes = (byte) Short.BYTES;
    this.byteBacking = null;
  }

  /** Convert from short => int backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToInt() {
    this.intBacking = new int[this.shortBacking.length];
    for (int i = 0; i < this.shortBacking.length; i++) {
      this.intBacking[i] = this.shortBacking[i];
    }
    this.cellSizeBytes = (byte) Integer.BYTES;
    this.shortBacking = null;
  }
  /** convert from int => long backing array. */
  @SuppressWarnings("NullAway")
  private void resizeToLong() {
    this.longBacking = new long[this.intBacking.length];
    for (int i = 0; i < this.intBacking.length; i++) {
      this.longBacking[i] = this.intBacking[i];
    }
    this.cellSizeBytes = (byte) Long.BYTES;
    this.intBacking = null;
  }
}
