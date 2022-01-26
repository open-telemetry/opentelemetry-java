/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

/**
 * Interface for use as backing data structure for exponential histogram buckets.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExponentialCounter {
  /**
   * The first index with a recording. May be negative.
   *
   * <p>Note: the returned value is not meaningful when isEmpty returns true.
   *
   * @return the first index with a recording.
   */
  int getIndexStart();

  /**
   * The last index with a recording. May be negative.
   *
   * <p>Note: the returned value is not meaningful when isEmpty returns true.
   *
   * @return The last index with a recording.
   */
  int getIndexEnd();

  /**
   * Persist new data at index, incrementing by delta amount.
   *
   * @param index The index of where to perform the incrementation.
   * @param delta How much to increment the index by.
   * @return success status.
   */
  boolean increment(int index, long delta);

  /**
   * Get the number of recordings for the given index.
   *
   * @return the number of recordings for the index, or 0 if the index is out of bounds.
   */
  long get(int index);

  /**
   * Boolean denoting if the backing structure has recordings or not.
   *
   * @return true if no recordings, false if at least one recording.
   */
  boolean isEmpty();

  /** Returns the maximum number of buckets allowed in this counter. */
  int getMaxSize();

  /** Resets all bucket counts to zero and resets index start/end tracking. */
  void clear();
}
