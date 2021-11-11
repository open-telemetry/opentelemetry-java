/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.AbstractList;
import java.util.List;

/**
 * A list of longs backed by, and exposing, an array of primitives. Values will be boxed on demand
 * when using standard List operations. Operations should generally use the static methods in this
 * class to operate directly on the backing array instead. The idea is that in almost all apps, the
 * list will only be accessed by our internal code, and if it does happen to be used elsewhere,
 * performance of on-demand boxing isn't prohibitive while still providing expected ergonomics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class PrimitiveLongList {

  /**
   * Returns a list that wraps the primitive array. Modifications in the array will be visible in
   * the list.
   */
  public static List<Long> wrap(long[] values) {
    return new LongListImpl(values);
  }

  /**
   * Returns a primitive array with the values of the list. The list should generally have been *
   * created with {@link PrimitiveLongList#wrap(long[])}.
   */
  public static long[] toArray(List<Long> list) {
    if (list instanceof LongListImpl) {
      return ((LongListImpl) list).values;
    }

    long[] values = new long[list.size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = list.get(i);
    }
    return values;
  }

  private static class LongListImpl extends AbstractList<Long> {

    private final long[] values;

    LongListImpl(long[] values) {
      this.values = values;
    }

    @Override
    public Long get(int index) {
      // If out of bounds, the array access will produce a perfectly fine IndexOutOfBoundsException.
      return values[index];
    }

    @Override
    public int size() {
      return values.length;
    }
  }

  private PrimitiveLongList() {}
}
