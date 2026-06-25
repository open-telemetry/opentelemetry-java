/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

/**
 * A {@link ComponentId} where the component type is one of {@link ExporterType}.
 *
 * <p>Copied from {@code io.opentelemetry.sdk.common.internal.StandardComponentId} to avoid shared
 * internal code.
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
    ZIPKIN_HTTP_SPAN_EXPORTER("zipkin_http_span_exporter", Signal.SPAN),
    /**
     * Has the same semconv attribute value as ZIPKIN_HTTP_SPAN_EXPORTER, but we still use a
     * different enum value for now because they produce separate legacy metrics.
     */
    ZIPKIN_HTTP_JSON_SPAN_EXPORTER("zipkin_http_span_exporter", Signal.SPAN);

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
