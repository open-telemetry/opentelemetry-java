/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Sanitizes a metric or label name. */
class NameSanitizer implements Function<String, String> {

  static final NameSanitizer INSTANCE = new NameSanitizer();

  private static final Pattern SANITIZE_PREFIX_PATTERN = Pattern.compile("^[^a-zA-Z_:]");
  private static final Pattern SANITIZE_BODY_PATTERN = Pattern.compile("[^a-zA-Z0-9_:]");

  private final Function<String, String> delegate;
  private final Map<String, String> cache = new ConcurrentHashMap<>();

  NameSanitizer() {
    this(NameSanitizer::sanitizeMetricName);
  }

  // visible for testing
  NameSanitizer(Function<String, String> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String apply(String labelName) {
    return cache.computeIfAbsent(labelName, delegate);
  }

  private static String sanitizeMetricName(String metricName) {
    return SANITIZE_BODY_PATTERN
        .matcher(SANITIZE_PREFIX_PATTERN.matcher(metricName).replaceFirst("_"))
        .replaceAll("_");
  }
}
