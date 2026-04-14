/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import com.google.common.base.Strings;
import com.google.protobuf.AbstractMessageLite;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsPartialSuccess;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.profiles.v1development.ExportProfilesPartialSuccess;
import io.opentelemetry.proto.collector.profiles.v1development.ExportProfilesServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.profiles.ProfileData;
import io.opentelemetry.sdk.profiles.ProfileExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface TelemetryExporter<T> extends AutoCloseable {

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

      @Override
      public AbstractMessageLite<?, ?> exportResponse(int minimumSize) {
        if (minimumSize == 0) {
          return ExportTraceServiceResponse.getDefaultInstance();
        }
        return ExportTraceServiceResponse.newBuilder()
            .setPartialSuccess(
                ExportTracePartialSuccess.newBuilder()
                    .setErrorMessage(Strings.repeat("x", minimumSize))
                    .build())
            .build();
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

      @Override
      public AbstractMessageLite<?, ?> exportResponse(int minimumSize) {
        if (minimumSize == 0) {
          return ExportMetricsServiceResponse.getDefaultInstance();
        }
        return ExportMetricsServiceResponse.newBuilder()
            .setPartialSuccess(
                ExportMetricsPartialSuccess.newBuilder()
                    .setErrorMessage(Strings.repeat("x", minimumSize))
                    .build())
            .build();
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

      @Override
      public AbstractMessageLite<?, ?> exportResponse(int minimumSize) {
        if (minimumSize == 0) {
          return ExportLogsServiceResponse.getDefaultInstance();
        }
        return ExportLogsServiceResponse.newBuilder()
            .setPartialSuccess(
                ExportLogsPartialSuccess.newBuilder()
                    .setErrorMessage(Strings.repeat("x", minimumSize))
                    .build())
            .build();
      }
    };
  }

  /** Wraps a ProfilerExporter. */
  static TelemetryExporter<ProfileData> wrap(ProfileExporter exporter) {
    return new TelemetryExporter<ProfileData>() {
      @Override
      public Object unwrap() {
        return exporter;
      }

      @Override
      public CompletableResultCode export(Collection<ProfileData> items) {
        return exporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        return exporter.shutdown();
      }

      @Override
      public AbstractMessageLite<?, ?> exportResponse(int minimumSize) {
        if (minimumSize == 0) {
          return ExportProfilesServiceResponse.getDefaultInstance();
        }
        return ExportProfilesServiceResponse.newBuilder()
            .setPartialSuccess(
                ExportProfilesPartialSuccess.newBuilder()
                    .setErrorMessage(Strings.repeat("x", minimumSize))
                    .build())
            .build();
      }
    };
  }

  Object unwrap();

  CompletableResultCode export(Collection<T> items);

  CompletableResultCode shutdown();

  AbstractMessageLite<?, ?> exportResponse(int minimumSize);

  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
