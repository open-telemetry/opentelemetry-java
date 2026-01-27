/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.Signal;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Implements health metrics for exporters which were defined prior to the standardization in
 * semantic conventions.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class LegacyExporterMetrics implements ExporterMetrics {

  private static final AttributeKey<String> ATTRIBUTE_KEY_TYPE = stringKey("type");
  private static final AttributeKey<Boolean> ATTRIBUTE_KEY_SUCCESS = booleanKey("success");

  private final Supplier<MeterProvider> meterProviderSupplier;
  private final String exporterName;
  private final String transportName;
  private final Attributes seenAttrs;
  private final Attributes successAttrs;
  private final Attributes failedAttrs;

  /** Access via {@link #seen()}. */
  @Nullable private volatile LongCounter seen;

  /** Access via {@link #exported()} . */
  @Nullable private volatile LongCounter exported;

  LegacyExporterMetrics(
      Supplier<MeterProvider> meterProviderSupplier,
      StandardComponentId.ExporterType exporterType) {
    this.meterProviderSupplier = meterProviderSupplier;
    this.exporterName = getExporterName(exporterType);
    this.transportName = getTransportName(exporterType);
    this.seenAttrs =
        Attributes.builder().put(ATTRIBUTE_KEY_TYPE, getTypeString(exporterType.signal())).build();
    this.successAttrs = this.seenAttrs.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, true).build();
    this.failedAttrs = this.seenAttrs.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, false).build();
  }

  public static boolean isSupportedType(StandardComponentId.ExporterType exporterType) {
    switch (exporterType) {
      case OTLP_GRPC_SPAN_EXPORTER:
      case OTLP_HTTP_SPAN_EXPORTER:
      case OTLP_HTTP_JSON_SPAN_EXPORTER:
      case ZIPKIN_HTTP_SPAN_EXPORTER:
      case ZIPKIN_HTTP_JSON_SPAN_EXPORTER:
      case OTLP_GRPC_LOG_EXPORTER:
      case OTLP_HTTP_LOG_EXPORTER:
      case OTLP_HTTP_JSON_LOG_EXPORTER:
      case OTLP_GRPC_METRIC_EXPORTER:
      case OTLP_HTTP_METRIC_EXPORTER:
      case OTLP_HTTP_JSON_METRIC_EXPORTER:
        return true;
      default:
        return false;
    }
  }

  private static String getTypeString(Signal signal) {
    switch (signal) {
      case SPAN:
        return "span";
      case LOG:
        return "log";
      case METRIC:
        return "metric";
      case PROFILE:
        throw new IllegalArgumentException("Profiles are not supported");
    }
    throw new IllegalArgumentException("Unhandled signal type: " + signal);
  }

  private static String getExporterName(StandardComponentId.ExporterType exporterType) {
    switch (exporterType) {
      case OTLP_GRPC_SPAN_EXPORTER:
      case OTLP_HTTP_SPAN_EXPORTER:
      case OTLP_HTTP_JSON_SPAN_EXPORTER:
      case OTLP_GRPC_LOG_EXPORTER:
      case OTLP_HTTP_LOG_EXPORTER:
      case OTLP_HTTP_JSON_LOG_EXPORTER:
      case OTLP_GRPC_METRIC_EXPORTER:
      case OTLP_HTTP_METRIC_EXPORTER:
      case OTLP_HTTP_JSON_METRIC_EXPORTER:
        return "otlp";
      case ZIPKIN_HTTP_SPAN_EXPORTER:
      case ZIPKIN_HTTP_JSON_SPAN_EXPORTER:
        return "zipkin";
      case OTLP_GRPC_PROFILES_EXPORTER:
        throw new IllegalArgumentException("Profiles are not supported");
    }
    throw new IllegalArgumentException("Not a supported exporter type: " + exporterType);
  }

  private static String getTransportName(StandardComponentId.ExporterType exporterType) {
    switch (exporterType) {
      case OTLP_GRPC_SPAN_EXPORTER:
      case OTLP_GRPC_LOG_EXPORTER:
      case OTLP_GRPC_METRIC_EXPORTER:
        return "grpc";
      case OTLP_HTTP_SPAN_EXPORTER:
      case OTLP_HTTP_LOG_EXPORTER:
      case OTLP_HTTP_METRIC_EXPORTER:
      case ZIPKIN_HTTP_SPAN_EXPORTER:
        return "http";
      case OTLP_HTTP_JSON_SPAN_EXPORTER:
      case OTLP_HTTP_JSON_LOG_EXPORTER:
      case OTLP_HTTP_JSON_METRIC_EXPORTER:
      case ZIPKIN_HTTP_JSON_SPAN_EXPORTER:
        return "http-json";
      case OTLP_GRPC_PROFILES_EXPORTER:
        throw new IllegalArgumentException("Profiles are not supported");
    }
    throw new IllegalArgumentException("Not a supported exporter type: " + exporterType);
  }

  /** Record number of records seen. */
  private void addSeen(long value) {
    seen().add(value, seenAttrs);
  }

  /** Record number of records which successfully exported. */
  private void addSuccess(long value) {
    exported().add(value, successAttrs);
  }

  /** Record number of records which failed to export. */
  private void addFailed(long value) {
    exported().add(value, failedAttrs);
  }

  private LongCounter seen() {
    LongCounter seen = this.seen;
    if (seen == null || SemConvExporterMetrics.isNoop(seen)) {
      seen = meter().counterBuilder(exporterName + ".exporter.seen").build();
      this.seen = seen;
    }
    return seen;
  }

  private LongCounter exported() {
    LongCounter exported = this.exported;
    if (exported == null || SemConvExporterMetrics.isNoop(exported)) {
      exported = meter().counterBuilder(exporterName + ".exporter.exported").build();
      this.exported = exported;
    }
    return exported;
  }

  private Meter meter() {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    return meterProvider.get("io.opentelemetry.exporters." + exporterName + "-" + transportName);
  }

  @Override
  public ExporterMetrics.Recording startRecordingExport(int itemCount) {
    return new Recording(itemCount);
  }

  private class Recording extends ExporterMetrics.Recording {

    private final int itemCount;

    private Recording(int itemCount) {
      this.itemCount = itemCount;
      addSeen(itemCount);
    }

    @Override
    protected void doFinish(@Nullable String errorType, Attributes requestAttributes) {
      if (errorType != null) {
        addFailed(itemCount);
      } else {
        addSuccess(itemCount);
      }
    }
  }
}
