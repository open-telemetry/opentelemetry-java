/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A SampleCompositionValue represents the per-observation parts of an aggregation of observed data.
 * Observations (samples) having the same key can be merged by appending their distinct fields to
 * the composed value.
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 */
public class SampleCompositionValue {

  private final List<Long> values = new ArrayList<>();
  private final List<Long> timestamps = new ArrayList<>();

  public List<Long> getValues() {
    return Collections.unmodifiableList(values);
  }

  public List<Long> getTimestamps() {
    return Collections.unmodifiableList(timestamps);
  }

  /**
   * Add a new observation to the collection.
   *
   * <p>Note that, whilst not enforced by the API, it is required that all observations in a
   * collection share the same 'shape'. That is, they have either a value without timestamp, a
   * timestamp without value, or both timestamp and value. Thus each array (values, timestamps) in
   * the collection is either zero length, or the same length as the other.
   *
   * @param value the observed data point.
   * @param timestamp the time of the observation.
   */
  public void add(@Nullable Long value, @Nullable Long timestamp) {
    if (value != null) {
      values.add(value);
    }
    if (timestamp != null) {
      timestamps.add(timestamp);
    }
  }
}
