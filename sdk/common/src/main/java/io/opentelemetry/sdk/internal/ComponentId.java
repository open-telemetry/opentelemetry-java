/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * The component id used for SDK health metrics. This corresponds to the otel.component.name and
 * otel.component.id semconv attributes.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class ComponentId {

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public enum StandardExporterType {
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
    ZIPKIN_HTTP_JSON_SPAN_EXPORTER("zipkin_http_span_exporter", Signal.SPAN);

    private final String value;
    private final Signal signal;

    StandardExporterType(String value, Signal signal) {
      this.value = value;
      this.signal = signal;
    }

    @Override
    public String toString() {
      return value;
    }

    public Signal signal() {
      return signal;
    }
  }

  private ComponentId() {}

  public abstract String getTypeName();

  public abstract String getComponentName();

  public void put(AttributesBuilder attributes) {
    attributes.put(SemConvAttributes.OTEL_COMPONENT_TYPE, getTypeName());
    attributes.put(SemConvAttributes.OTEL_COMPONENT_NAME, getComponentName());
  }

  private static class Lazy extends ComponentId {

    private static final Map<String, AtomicInteger> nextIdCounters = new ConcurrentHashMap<>();

    private final String componentType;
    @Nullable private volatile String componentName = null;

    private Lazy(String componentType) {
      this.componentType = componentType;
    }

    @Override
    public String getTypeName() {
      return componentType;
    }

    @Override
    public String getComponentName() {
      if (componentName == null) {
        synchronized (this) {
          if (componentName == null) {
            int id =
                nextIdCounters
                    .computeIfAbsent(componentType, k -> new AtomicInteger(0))
                    .getAndIncrement();
            componentName = componentType + "/" + id;
          }
        }
      }
      return componentName;
    }
  }

  public static ComponentId generateLazy(String componentType) {
    return new Lazy(componentType);
  }

  public static ComponentId generateLazy(StandardExporterType standardExporterType) {
    return new Lazy(standardExporterType.value);
  }
}
