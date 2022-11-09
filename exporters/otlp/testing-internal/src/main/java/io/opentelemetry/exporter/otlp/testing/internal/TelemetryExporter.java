/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

public interface TelemetryExporter<T> {

  /** Wraps a SpanExporter. */
  static TelemetryExporter<SpanData> wrap(SpanExporter exporter) {
    return new TelemetryExporter<SpanData>() {
      @Override
      public Object unwrap() {
        return exporter;
      }

      @Override
      public CompletableResultCode export(Collection<SpanData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        return exporter.shutdown();
      }
    };
  }

  /** Wraps a MetricExporter. */
  static TelemetryExporter<MetricData> wrap(MetricExporter exporter) {
    return new TelemetryExporter<MetricData>() {
      @Override
      public Object unwrap() {
        return exporter;
      }

      @Override
      public CompletableResultCode export(Collection<MetricData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        return exporter.shutdown();
      }
    };
  }

  /** Wraps a LogRecordExporter. */
  static TelemetryExporter<LogRecordData> wrap(LogRecordExporter exporter) {
    return new TelemetryExporter<LogRecordData>() {
      @Override
      public Object unwrap() {
        return exporter;
      }

      @Override
      public CompletableResultCode export(Collection<LogRecordData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        return exporter.shutdown();
      }
    };
  }

  Object unwrap();

  CompletableResultCode export(Collection<T> items);

  CompletableResultCode shutdown();
}
