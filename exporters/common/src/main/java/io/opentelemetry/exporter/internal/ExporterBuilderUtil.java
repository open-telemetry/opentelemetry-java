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
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Utilities for exporter builders.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExporterBuilderUtil {

  private static final Logger logger = Logger.getLogger(ExporterBuilderUtil.class.getName());

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
    String memoryModeStr = config.getString("otel.java.exporter.memory_mode");
    if (memoryModeStr == null) {
      memoryModeStr = config.getString("otel.java.experimental.exporter.memory_mode");
      if (memoryModeStr != null) {
        logger.warning(
            "otel.java.experimental.exporter.memory_mode was set but has been replaced with otel.java.exporter.memory_mode and will be removed in a future release");
      }
    }
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

  /**
   * Invoke the {@code aggregationTemporalitySelectorConsumer} with the configured {@link
   * AggregationTemporality}.
   */
  public static void configureOtlpAggregationTemporality(
      ConfigProperties config,
      Consumer<AggregationTemporalitySelector> aggregationTemporalitySelectorConsumer) {
    String temporalityStr = config.getString("otel.exporter.otlp.metrics.temporality.preference");
    if (temporalityStr == null) {
      return;
    }
    AggregationTemporalitySelector temporalitySelector;
    switch (temporalityStr.toLowerCase(Locale.ROOT)) {
      case "cumulative":
        temporalitySelector = AggregationTemporalitySelector.alwaysCumulative();
        break;
      case "delta":
        temporalitySelector = AggregationTemporalitySelector.deltaPreferred();
        break;
      case "lowmemory":
        temporalitySelector = AggregationTemporalitySelector.lowMemory();
        break;
      default:
        throw new ConfigurationException("Unrecognized aggregation temporality: " + temporalityStr);
    }
    aggregationTemporalitySelectorConsumer.accept(temporalitySelector);
  }

  public static void configureOtlpAggregationTemporality(
      StructuredConfigProperties config,
      Consumer<AggregationTemporalitySelector> aggregationTemporalitySelectorConsumer) {
    String temporalityStr = config.getString("temporality_preference");
    if (temporalityStr == null) {
      return;
    }
    AggregationTemporalitySelector temporalitySelector;
    switch (temporalityStr.toLowerCase(Locale.ROOT)) {
      case "cumulative":
        temporalitySelector = AggregationTemporalitySelector.alwaysCumulative();
        break;
      case "delta":
        temporalitySelector = AggregationTemporalitySelector.deltaPreferred();
        break;
      case "lowmemory":
        temporalitySelector = AggregationTemporalitySelector.lowMemory();
        break;
      default:
        throw new ConfigurationException("Unrecognized temporality_preference: " + temporalityStr);
    }
    aggregationTemporalitySelectorConsumer.accept(temporalitySelector);
  }

  /**
   * Invoke the {@code defaultAggregationSelectorConsumer} with the configured {@link
   * DefaultAggregationSelector}.
   */
  public static void configureOtlpHistogramDefaultAggregation(
      ConfigProperties config,
      Consumer<DefaultAggregationSelector> defaultAggregationSelectorConsumer) {
    String defaultHistogramAggregation =
        config.getString("otel.exporter.otlp.metrics.default.histogram.aggregation");
    if (defaultHistogramAggregation != null) {
      configureHistogramDefaultAggregation(
          defaultHistogramAggregation, defaultAggregationSelectorConsumer);
    }
  }

  /**
   * Invoke the {@code defaultAggregationSelectorConsumer} with the configured {@link
   * DefaultAggregationSelector}.
   */
  public static void configureOtlpHistogramDefaultAggregation(
      StructuredConfigProperties config,
      Consumer<DefaultAggregationSelector> defaultAggregationSelectorConsumer) {
    String defaultHistogramAggregation = config.getString("default_histogram_aggregation");
    if (defaultHistogramAggregation == null) {
      return;
    }
    if (AggregationUtil.aggregationName(Aggregation.base2ExponentialBucketHistogram())
        .equalsIgnoreCase(defaultHistogramAggregation)) {
      defaultAggregationSelectorConsumer.accept(
          DefaultAggregationSelector.getDefault()
              .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()));
    } else if (!AggregationUtil.aggregationName(explicitBucketHistogram())
        .equalsIgnoreCase(defaultHistogramAggregation)) {
      throw new ConfigurationException(
          "Unrecognized default_histogram_aggregation: " + defaultHistogramAggregation);
    }
  }

  private ExporterBuilderUtil() {}
}
