/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
class ImmutableLongArray {
  private static final ImmutableLongArray EMPTY = new ImmutableLongArray(new long[0]);

  /** Returns an immutable array containing a single value. */
  public static ImmutableLongArray of(long e0) {
    return new ImmutableLongArray(new long[] {e0});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableLongArray copyOf(long[] values) {
    return values.length == 0
        ? EMPTY
        : new ImmutableLongArray(Arrays.copyOf(values, values.length));
  }

  private final long[] array;

  private ImmutableLongArray(long[] array) {
    this.array = array;
  }

  /** Returns a copy of the underlying data as list. */
  public List<Long> toList() {
    List<Long> result = new ArrayList<>(array.length);
    for (long v : array) {
      result.add(v);
    }
    return result;
  }

  /** Returns the number of values in this array. */
  public int length() {
    return array.length;
  }

  /**
   * Returns the {@code long} value present at the given index.
   *
   * @throws IndexOutOfBoundsException if {@code index} is negative, or greater than or equal to
   *     {@link #length}
   */
  public long get(int index) {
    return array[index];
  }

  /**
   * Returns {@code true} if {@code object} is an {@code ImmutableLongArray} containing the same
   * values as this one, in the same order.
   */
  @Override
  public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof ImmutableLongArray)) {
      return false;
    }
    ImmutableLongArray that = (ImmutableLongArray) object;
    if (this.length() != that.length()) {
      return false;
    }
    for (int i = 0; i < length(); i++) {
      if (this.get(i) != that.get(i)) {
        return false;
      }
    }
    return true;
  }

  /** Returns an unspecified hash code for the contents of this immutable array. */
  @Override
  public int hashCode() {
    int hash = 1;
    for (long value : array) {
      hash *= 31;
      hash += (int) (value ^ (value >>> 32));
    }
    return hash;
  }

  /**
   * Returns a string representation of this array in the same form as {@link
   * Arrays#toString(long[])}, for example {@code "[1, 2, 3]"}.
   */
  @Override
  public String toString() {
    if (length() == 0) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder(length() * 5); // rough estimate is fine
    builder.append('[').append(array[0]);

    for (int i = 1; i < length(); i++) {
      builder.append(", ").append(array[i]);
    }
    builder.append(']');
    return builder.toString();
  }
}
