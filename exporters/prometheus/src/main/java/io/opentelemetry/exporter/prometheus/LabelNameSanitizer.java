/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.prometheus.client.Collector;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Used to convert a label keys to a label names. Sanitizes the label keys. Not thread safe. */
public class LabelNameSanitizer implements Function<String, String> {

  private final Function<String, String> delegate;
  private final Map<String, String> cache = new HashMap<>();

  public LabelNameSanitizer() {
    this(Collector::sanitizeMetricName);
  }

  // visible for testing
  LabelNameSanitizer(Function<String, String> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String apply(String labelName) {
    return cache.computeIfAbsent(labelName, delegate);
  }
}
