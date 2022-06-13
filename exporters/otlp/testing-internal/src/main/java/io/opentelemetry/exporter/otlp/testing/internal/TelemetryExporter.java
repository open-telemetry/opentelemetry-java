/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;

public interface TelemetryExporter<T> {

  /** Wraps a SpanExporter. */
  static TelemetryExporter<SpanData> wrap(SpanExporter exporter, Closeable onShutdown) {
    return new TelemetryExporter<SpanData>() {
      @Override
      public CompletableResultCode export(Collection<SpanData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        try {
          onShutdown.close();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        return exporter.shutdown();
      }
    };
  }

  /** Wraps a MetricExporter. */
  static TelemetryExporter<MetricData> wrap(MetricExporter exporter, Closeable onShutdown) {
    return new TelemetryExporter<MetricData>() {
      @Override
      public CompletableResultCode export(Collection<MetricData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        try {
          onShutdown.close();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        return exporter.shutdown();
      }
    };
  }

  /** Wraps a LogExporter. */
  static TelemetryExporter<LogData> wrap(LogExporter exporter, Closeable onShutdown) {
    return new TelemetryExporter<LogData>() {
      @Override
      public CompletableResultCode export(Collection<LogData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        try {
          onShutdown.close();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        return exporter.shutdown();
      }
    };
  }

  CompletableResultCode export(Collection<T> items);

  CompletableResultCode shutdown();
}
