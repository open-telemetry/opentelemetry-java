/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Utilities for exporter builders.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExporterBuilderUtil {

  /** Validate OTLP endpoint. */
  public static URI validateEndpoint(String endpoint) {
    URI uri;
    try {
      uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
    }

    if (uri.getScheme() == null
        || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }
    return uri;
  }

  /** Invoke the {@code memoryModeConsumer} with the configured {@link MemoryMode}. */
  public static void configureExporterMemoryMode(
      ConfigProperties config, Consumer<MemoryMode> memoryModeConsumer) {
    String memoryModeStr = config.getString("otel.java.experimental.exporter.memory_mode");
    if (memoryModeStr == null) {
      return;
    }
    MemoryMode memoryMode;
    try {
      memoryMode = MemoryMode.valueOf(memoryModeStr.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException("Unrecognized memory mode: " + memoryModeStr, e);
    }
    memoryModeConsumer.accept(memoryMode);
  }

  /** Invoke the {@code memoryModeConsumer} with the configured {@link MemoryMode}. */
  public static void configureExporterMemoryMode(
      StructuredConfigProperties config, Consumer<MemoryMode> memoryModeConsumer) {
    String memoryModeStr = config.getString("memory_mode");
    if (memoryModeStr == null) {
      return;
    }
    MemoryMode memoryMode;
    try {
      memoryMode = MemoryMode.valueOf(memoryModeStr.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException("Unrecognized memory_mode: " + memoryModeStr, e);
    }
    memoryModeConsumer.accept(memoryMode);
  }

  /**
   * Invoke the {@code defaultAggregationSelectorConsumer} with the configured {@link
   * DefaultAggregationSelector}.
   */
  public static void configureHistogramDefaultAggregation(
      String defaultHistogramAggregation,
      Consumer<DefaultAggregationSelector> defaultAggregationSelectorConsumer) {
    if (AggregationUtil.aggregationName(Aggregation.base2ExponentialBucketHistogram())
        .equalsIgnoreCase(defaultHistogramAggregation)) {
      defaultAggregationSelectorConsumer.accept(
          DefaultAggregationSelector.getDefault()
              .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()));
    } else if (!AggregationUtil.aggregationName(explicitBucketHistogram())
        .equalsIgnoreCase(defaultHistogramAggregation)) {
      throw new ConfigurationException(
          "Unrecognized default histogram aggregation: " + defaultHistogramAggregation);
    }
  }

  private ExporterBuilderUtil() {}
}
