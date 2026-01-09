/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

/**
 * A {@link ComponentId} where the component type is one of {@link ExporterType}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class StandardComponentId extends ComponentId.Lazy {

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public enum ExporterType {
    OTLP_GRPC_SPAN_EXPORTER("otlp_grpc_span_exporter", Signal.SPAN),
    OTLP_HTTP_SPAN_EXPORTER("otlp_http_span_exporter", Signal.SPAN),
    OTLP_HTTP_JSON_SPAN_EXPORTER("otlp_http_json_span_exporter", Signal.SPAN),
    OTLP_GRPC_LOG_EXPORTER("otlp_grpc_log_exporter", Signal.LOG),
    OTLP_HTTP_LOG_EXPORTER("otlp_http_log_exporter", Signal.LOG),
    OTLP_HTTP_JSON_LOG_EXPORTER("otlp_http_json_log_exporter", Signal.LOG),
    OTLP_GRPC_METRIC_EXPORTER("otlp_grpc_metric_exporter", Signal.METRIC),
    OTLP_HTTP_METRIC_EXPORTER("otlp_http_metric_exporter", Signal.METRIC),
    OTLP_HTTP_JSON_METRIC_EXPORTER("otlp_http_json_metric_exporter", Signal.METRIC),
    ZIPKIN_HTTP_SPAN_EXPORTER("zipkin_http_span_exporter", Signal.SPAN),
    /**
     * Has the same semconv attribute value as ZIPKIN_HTTP_SPAN_EXPORTER, but we still use a
     * different enum value for now because they produce separate legacy metrics.
     */
    ZIPKIN_HTTP_JSON_SPAN_EXPORTER("zipkin_http_span_exporter", Signal.SPAN),

    OTLP_GRPC_PROFILES_EXPORTER("TBD", Signal.PROFILE); // TODO: not yet standardized in semconv

    final String value;
    private final Signal signal;

    ExporterType(String value, Signal signal) {
      this.value = value;
      this.signal = signal;
    }

    public Signal signal() {
      return signal;
    }
  }

  private final ExporterType standardType;

  StandardComponentId(ExporterType standardType) {
    super(standardType.value);
    this.standardType = standardType;
  }

  public ExporterType getStandardType() {
    return standardType;
  }
}
