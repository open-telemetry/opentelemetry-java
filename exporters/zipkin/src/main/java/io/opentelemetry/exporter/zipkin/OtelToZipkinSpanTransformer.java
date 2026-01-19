/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.net.InetAddress;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import zipkin2.Endpoint;
import zipkin2.Span;

/**
 * This class is responsible for transforming an OpenTelemetry SpanData instance into an instance of
 * a Zipkin Span. It is based, in part, on code from
 * https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin
 */
final class OtelToZipkinSpanTransformer {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  private static final AttributeKey<String> PEER_SERVICE = stringKey("peer.service");
  private static final AttributeKey<String> SERVER_SOCKET_ADDRESS =
      stringKey("server.socket.address");
  private static final AttributeKey<Long> SERVER_SOCKET_PORT = longKey("server.socket.port");

  static final String KEY_INSTRUMENTATION_SCOPE_NAME = "otel.scope.name";
  static final String KEY_INSTRUMENTATION_SCOPE_VERSION = "otel.scope.version";
  static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
  static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";
  static final String OTEL_DROPPED_ATTRIBUTES_COUNT = "otel.dropped_attributes_count";
  static final String OTEL_DROPPED_EVENTS_COUNT = "otel.dropped_events_count";
  static final String OTEL_STATUS_CODE = "otel.status_code";
  static final AttributeKey<String> STATUS_ERROR = stringKey("error");
  private final Supplier<InetAddress> ipAddressSupplier;

  /**
   * Creates an instance of an OtelToZipkinSpanTransformer with the given Supplier that can produce
   * an InetAddress, which may be null. This value from this Supplier will be used when creating the
   * local zipkin Endpoint for each Span. The default implementation uses
   * LocalInetAddressSupplier.getInstance().
   *
   * @param ipAddressSupplier - A Supplier of an InetAddress.
   */
  static OtelToZipkinSpanTransformer create(Supplier<InetAddress> ipAddressSupplier) {
    return new OtelToZipkinSpanTransformer(ipAddressSupplier);
  }

  /**
   * Creates an instance of an OtelToZipkinSpanTransformer with the given Supplier that can produce
   * an InetAddress. Supplier may return null. This value from this Supplier will be used when
   * creating the local zipkin Endpoint for each Span.
   *
   * @param ipAddressSupplier - A Supplier of an InetAddress, which can be null
   */
  private OtelToZipkinSpanTransformer(Supplier<InetAddress> ipAddressSupplier) {
    this.ipAddressSupplier = ipAddressSupplier;
  }

  /**
   * Creates an instance of a Zipkin Span from an OpenTelemetry SpanData instance.
   *
   * @param spanData an OpenTelemetry spanData instance
   * @return a new Zipkin Span
   */
  Span generateSpan(SpanData spanData) {
    long startTimestamp = toEpochMicros(spanData.getStartEpochNanos());
    long endTimestamp = toEpochMicros(spanData.getEndEpochNanos());

    Span.Builder spanBuilder =
        Span.newBuilder()
            .traceId(spanData.getTraceId())
            .id(spanData.getSpanId())
            .kind(toSpanKind(spanData))
            .name(spanData.getName())
            .timestamp(toEpochMicros(spanData.getStartEpochNanos()))
            .duration(Math.max(1, endTimestamp - startTimestamp))
            .localEndpoint(getLocalEndpoint(spanData))
            .remoteEndpoint(getRemoteEndpoint(spanData));

    if (spanData.getParentSpanContext().isValid()) {
      spanBuilder.parentId(spanData.getParentSpanId());
    }

    Attributes spanAttributes = spanData.getAttributes();
    spanAttributes.forEach(
        (key, value) -> spanBuilder.putTag(key.getKey(), valueToString(key, value)));
    int droppedAttributes = spanData.getTotalAttributeCount() - spanAttributes.size();
    if (droppedAttributes > 0) {
      spanBuilder.putTag(OTEL_DROPPED_ATTRIBUTES_COUNT, String.valueOf(droppedAttributes));
    }

    StatusData status = spanData.getStatus();

    // include status code & error.
    if (status.getStatusCode() != StatusCode.UNSET) {
      spanBuilder.putTag(OTEL_STATUS_CODE, status.getStatusCode().toString());

      // add the error tag, if it isn't already in the source span.
      if (status.getStatusCode() == StatusCode.ERROR && spanAttributes.get(STATUS_ERROR) == null) {
        spanBuilder.putTag(STATUS_ERROR.getKey(), nullToEmpty(status.getDescription()));
      }
    }

    InstrumentationScopeInfo instrumentationScopeInfo = spanData.getInstrumentationScopeInfo();

    if (!instrumentationScopeInfo.getName().isEmpty()) {
      spanBuilder.putTag(KEY_INSTRUMENTATION_SCOPE_NAME, instrumentationScopeInfo.getName());
      // Include instrumentation library name for backwards compatibility
      spanBuilder.putTag(KEY_INSTRUMENTATION_LIBRARY_NAME, instrumentationScopeInfo.getName());
    }
    if (instrumentationScopeInfo.getVersion() != null) {
      spanBuilder.putTag(KEY_INSTRUMENTATION_SCOPE_VERSION, instrumentationScopeInfo.getVersion());
      // Include instrumentation library name for backwards compatibility
      spanBuilder.putTag(
          KEY_INSTRUMENTATION_LIBRARY_VERSION, instrumentationScopeInfo.getVersion());
    }

    for (EventData eventData : spanData.getEvents()) {
      String annotation = EventDataToAnnotation.apply(eventData);
      spanBuilder.addAnnotation(toEpochMicros(eventData.getEpochNanos()), annotation);
    }
    int droppedEvents = spanData.getTotalRecordedEvents() - spanData.getEvents().size();
    if (droppedEvents > 0) {
      spanBuilder.putTag(OTEL_DROPPED_EVENTS_COUNT, String.valueOf(droppedEvents));
    }

    return spanBuilder.build();
  }

  private static String nullToEmpty(@Nullable String value) {
    return value != null ? value : "";
  }

  private Endpoint getLocalEndpoint(SpanData spanData) {
    Attributes resourceAttributes = spanData.getResource().getAttributes();

    Endpoint.Builder endpoint = Endpoint.newBuilder();
    endpoint.ip(ipAddressSupplier.get());

    // use the service.name from the Resource, if it's been set.
    String serviceNameValue = resourceAttributes.get(SERVICE_NAME);
    if (serviceNameValue == null) {
      serviceNameValue = Resource.getDefault().getAttribute(SERVICE_NAME);
    }
    // In practice should never be null unless the default Resource spec is changed.
    if (serviceNameValue != null) {
      endpoint.serviceName(serviceNameValue);
    }
    return endpoint.build();
  }

  @Nullable
  private static Endpoint getRemoteEndpoint(SpanData spanData) {
    if (spanData.getKind() == SpanKind.CLIENT || spanData.getKind() == SpanKind.PRODUCER) {
      // TODO: Implement fallback mechanism:
      // https://opentelemetry.io/docs/reference/specification/trace/sdk_exporters/zipkin/#otlp---zipkin
      Attributes attributes = spanData.getAttributes();
      String serviceName = attributes.get(PEER_SERVICE);

      if (serviceName != null) {
        Endpoint.Builder endpoint = Endpoint.newBuilder();
        endpoint.serviceName(serviceName);
        endpoint.ip(attributes.get(SERVER_SOCKET_ADDRESS));
        Long port = attributes.get(SERVER_SOCKET_PORT);
        if (port != null) {
          endpoint.port(port.intValue());
        }

        return endpoint.build();
      }
    }

    return null;
  }

  @Nullable
  private static Span.Kind toSpanKind(SpanData spanData) {
    switch (spanData.getKind()) {
      case SERVER:
        return Span.Kind.SERVER;
      case CLIENT:
        return Span.Kind.CLIENT;
      case PRODUCER:
        return Span.Kind.PRODUCER;
      case CONSUMER:
        return Span.Kind.CONSUMER;
      case INTERNAL:
        return null;
    }
    return null;
  }

  private static long toEpochMicros(long epochNanos) {
    return NANOSECONDS.toMicros(epochNanos);
  }

  private static String valueToString(AttributeKey<?> key, Object attributeValue) {
    AttributeType type = key.getType();
    switch (type) {
      case STRING:
      case BOOLEAN:
      case LONG:
      case DOUBLE:
        return String.valueOf(attributeValue);
      case STRING_ARRAY:
      case BOOLEAN_ARRAY:
      case LONG_ARRAY:
      case DOUBLE_ARRAY:
        return commaSeparated((List<?>) attributeValue);
      case VALUE:
        return ((Value<?>) attributeValue).toProtoJson();
    }
    throw new IllegalStateException("Unknown attribute type: " + type);
  }

  private static String commaSeparated(List<?> values) {
    StringBuilder builder = new StringBuilder();
    for (Object value : values) {
      if (builder.length() != 0) {
        builder.append(',');
      }
      builder.append(value);
    }
    return builder.toString();
  }
}
