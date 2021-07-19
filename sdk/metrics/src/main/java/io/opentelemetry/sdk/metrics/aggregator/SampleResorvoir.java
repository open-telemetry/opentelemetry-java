/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A resorvoir of samples of a given size.
 *
 * <p>This will allocate two atomic arrays of the resorvoir size and swap between them during
 * accumulation.
 */
public class SampleResorvoir<T> {
  private final AtomicReference<AtomicReferenceArray<T>> activeSamples;
  private AtomicReferenceArray<T> storedSamples;
  private final AtomicInteger count;
  // Note: we want a low-contention random in practice, something like ThreadLocalRandom.
  private final Random random;

  public SampleResorvoir(int size) {
    this(size, new Random());
  }

  public SampleResorvoir(int size, Random random) {
    this.activeSamples = new AtomicReference<>(new AtomicReferenceArray<>(size));
    this.storedSamples = new AtomicReferenceArray<>(size);
    this.count = new AtomicInteger();
    this.random = random;
  }

  /** Offers a new value to be sampled in the resorvoir. */
  public final void offer(T next) {
    // This is NOT the base case.
    int idx = random.nextInt(this.count.getAndIncrement());
    AtomicReferenceArray<T> current = activeSamples.get();
    if (idx < current.length()) {
      current.lazySet(idx, next);
    }
  }

  /**
   * Swaps the current resorvior for the next one, and builds a new list with the current resorvoir
   * results.
   */
  public final List<T> accumulateThenReset() {
    // Only lock other threads from doing this.
    synchronized (this) {
      // Swap the current storage.
      this.storedSamples = activeSamples.getAndSet(this.storedSamples);
      // It's ok if we're a bit slow here, just makes resorvior skip a sample.
      this.count.lazySet(0);

      // Now return the resorvior values.
      int size = this.storedSamples.length();
      List<T> result = new ArrayList<>(size);
      for (int i = 0; i < size; ++i) {
        T value = this.storedSamples.getAndSet(i, null);
        if (value != null) {
          result.add(value);
        }
      }
      return result;
    }
  }
}
