/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.Map;
import java.util.Set;

/**
 * Synchronous recording of delta-accumulated measurements.
 *
 * <p>This stores in-progress metric values that haven't been exported yet.
 */
class DeltaAccumulation<T> {
  private final Map<Attributes, T> recording;
  private final Set<CollectionHandle> readers;

  DeltaAccumulation(Map<Attributes, T> recording) {
    this.recording = recording;
    this.readers = CollectionHandle.mutableSet();
  }

  /** Returns true if this accumulation was read by the {@link CollectionHandle}. */
  boolean wasReadBy(CollectionHandle handle) {
    return readers.contains(handle);
  }

  /** Returns true if all readers in the given set have read this accumulation. */
  boolean wasReadByAll(Set<CollectionHandle> handles) {
    return readers.containsAll(handles);
  }

  /**
   * Reads the current delta accumulation.
   *
   * @param handle The reader of the accumulation.
   * @return the accumulation.
   */
  Map<Attributes, T> read(CollectionHandle handle) {
    readers.add(handle);
    return recording;
  }
}
