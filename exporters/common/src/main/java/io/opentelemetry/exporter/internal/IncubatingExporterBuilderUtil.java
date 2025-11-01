/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Utilities for exporter builders.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class IncubatingExporterBuilderUtil {

  /** Invoke the {@code memoryModeConsumer} with the configured {@link MemoryMode}. */
  public static void configureExporterMemoryMode(
      DeclarativeConfigProperties config, Consumer<MemoryMode> memoryModeConsumer) {
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

  public static void configureOtlpAggregationTemporality(
      DeclarativeConfigProperties config,
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
      case "low_memory":
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
      DeclarativeConfigProperties config,
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

  private IncubatingExporterBuilderUtil() {}
}
