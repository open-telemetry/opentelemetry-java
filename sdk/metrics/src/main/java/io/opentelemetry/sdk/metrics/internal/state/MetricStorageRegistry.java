/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Responsible for storing metrics (by name) and returning access to input pipeline for instrument
 * wiring.
 */
public class MetricStorageRegistry {
  private final ConcurrentMap<String, MetricStorage> registry = new ConcurrentHashMap<>();

  /**
   * Returns a {@code Collection} view of the registered {@link MetricStorage}.
   *
   * @return a {@code Collection} view of the registered {@link MetricStorage}.
   */
  public Collection<MetricStorage> getMetrics() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }
}
