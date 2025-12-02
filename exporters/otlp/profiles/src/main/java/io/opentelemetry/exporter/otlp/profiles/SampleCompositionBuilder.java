/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.otlp.internal.data.ImmutableSampleData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Assembles a collection of state observations into a collection of SampleData objects i.e. proto
 * Sample messages.
 *
 * <p>Observations (samples or traces from a profiler) having the same key can be merged to save
 * space without loss of fidelity. On the wire, a single Sample, modelled in the API as a SampleData
 * object, comprises shared fields (the key) and per-occurrence fields (the value and timestamp).
 * This class maps the raw observations to the aggregations.
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 */
public class SampleCompositionBuilder {

  private final Map<SampleCompositionKey, SampleCompositionValue> map = new HashMap<>();

  /**
   * Constructs a new collection of SampleData instances based on the builder's value.
   *
   * @return a new {@code List<SampleData>}
   */
  public List<SampleData> build() {
    List<SampleData> result = new ArrayList<>(map.size());
    for (Map.Entry<SampleCompositionKey, SampleCompositionValue> entry : map.entrySet()) {
      SampleCompositionKey key = entry.getKey();
      SampleCompositionValue value = entry.getValue();
      SampleData sampleData =
          ImmutableSampleData.create(
              key.getStackIndex(),
              value.getValues(),
              key.getAttributeIndices(),
              key.getLinkIndex(),
              value.getTimestamps());
      result.add(sampleData);
    }

    return result;
  }

  /**
   * Adds a new observation to the collection.
   *
   * @param key the shared ('primary key') fields of the observation.
   * @param value the observed data point.
   * @param timestamp the time of the observation.
   */
  public void add(SampleCompositionKey key, @Nullable Long value, @Nullable Long timestamp) {
    SampleCompositionValue v = map.computeIfAbsent(key, key1 -> new SampleCompositionValue());
    v.add(value, timestamp);
  }
}
