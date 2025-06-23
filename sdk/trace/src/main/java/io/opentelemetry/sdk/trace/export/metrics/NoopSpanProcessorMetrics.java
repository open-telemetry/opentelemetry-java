/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import java.util.function.LongSupplier;
import javax.annotation.Nullable;

class NoopSpanProcessorMetrics implements SpanProcessorMetrics {

  static final NoopSpanProcessorMetrics INSTANCE = new NoopSpanProcessorMetrics();

  @Override
  public void recordSpansProcessed(long count, @Nullable String errorType) {}

  @Override
  public void recordSpansExportedSuccessfully(long count) {}

  @Override
  public void startRecordingQueueMetrics(
      LongSupplier queueSizeSupplier, LongSupplier queueCapacitySupplier) {}

  @Override
  public void close() {}
}
