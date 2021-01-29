/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.common;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ImmutableDoubleArray {
  private static final ImmutableDoubleArray EMPTY = new ImmutableDoubleArray(new double[0]);

  /** Returns the empty array. */
  public static ImmutableDoubleArray of() {
    return EMPTY;
  }

  /** Returns an immutable array containing a single value. */
  public static ImmutableDoubleArray of(double e0) {
    return new ImmutableDoubleArray(new double[] {e0});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray copyOf(double[] values) {
    return values.length == 0
        ? EMPTY
        : new ImmutableDoubleArray(Arrays.copyOf(values, values.length));
  }

  private final double[] array;

  private ImmutableDoubleArray(double[] array) {
    this.array = array;
  }

  /** Returns the number of values in this array. */
  public int length() {
    return array.length;
  }

  /**
   * Returns the {@code double} value present at the given index.
   *
   * @throws IndexOutOfBoundsException if {@code index} is negative, or greater than or equal to
   *     {@link #length}
   */
  public double get(int index) {
    return array[index];
  }

  /**
   * Returns {@code true} if {@code object} is an {@code ImmutableDoubleArray} containing the same
   * values as this one, in the same order.
   */
  @Override
  public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof ImmutableDoubleArray)) {
      return false;
    }
    ImmutableDoubleArray that = (ImmutableDoubleArray) object;
    return Arrays.equals(this.array, that.array);
  }

  /** Returns an unspecified hash code for the contents of this immutable array. */
  @Override
  public int hashCode() {
    int hash = 1;
    for (double value : array) {
      hash *= 31;
      hash += ((Double) value).hashCode();
    }
    return hash;
  }

  /**
   * Returns a string representation of this array in the same form as {@link
   * Arrays#toString(double[])}, for example {@code "[1, 2, 3]"}.
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
