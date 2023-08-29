/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.export.MemoryMode.IMMUTABLE_DATA;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class NoopMetricExporter implements MetricExporter {
  private final AggregationTemporality aggregationTemporality;
  private final Aggregation aggregation;
  private final MemoryMode memoryMode;

  NoopMetricExporter(AggregationTemporality aggregationTemporality, Aggregation aggregation) {
    this(aggregationTemporality, aggregation, IMMUTABLE_DATA);
  }

  /**
   * Create a {@link NoopMetricExporter} with aggregationTemporality, aggregation and memory mode.
   */
  public NoopMetricExporter(
      AggregationTemporality aggregationTemporality,
      Aggregation aggregation,
      MemoryMode memoryMode) {
    this.aggregationTemporality = aggregationTemporality;
    this.aggregation = aggregation;
    this.memoryMode = memoryMode;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return aggregation;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
  }

  @Override
  public MemoryMode getMemoryMode() {
    return memoryMode;
  }
}
