/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import zipkin2.Endpoint;
import zipkin2.Span;

/**
 * This class is responsible for transforming an OpenTelemetry SpanData instance into an instance of
 * a Zipkin Span. It is based, in part, on code from
 * https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin
 */
public class OtelToZipkinSpanTransformer implements Function<SpanData, Span> {

  static final String KEY_INSTRUMENTATION_SCOPE_NAME = "otel.scope.name";
  static final String KEY_INSTRUMENTATION_SCOPE_VERSION = "otel.scope.version";
  static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
  static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";
  static final String OTEL_DROPPED_ATTRIBUTES_COUNT = "otel.dropped_attributes_count";
  static final String OTEL_DROPPED_EVENTS_COUNT = "otel.dropped_events_count";
  static final String OTEL_STATUS_CODE = "otel.status_code";
  public static final Logger logger = Logger.getLogger(ZipkinSpanExporter.class.getName());
  static final AttributeKey<String> STATUS_ERROR = stringKey("error");
  @Nullable private final InetAddress localAddress;

  public OtelToZipkinSpanTransformer() {
    this(produceLocalIp());
  }

  public OtelToZipkinSpanTransformer(@Nullable InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  @Override
  public Span apply(SpanData spanData) {
    return generateSpan(spanData);
  }

  Span generateSpan(SpanData spanData) {
    Endpoint endpoint = getEndpoint(spanData);

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
            .localEndpoint(endpoint);

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

    for (EventData annotation : spanData.getEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }
    int droppedEvents = spanData.getTotalRecordedEvents() - spanData.getEvents().size();
    if (droppedEvents > 0) {
      spanBuilder.putTag(OTEL_DROPPED_EVENTS_COUNT, String.valueOf(droppedEvents));
    }

    return spanBuilder.build();
  }

  private static String nullToEmpty(String value) {
    return value != null ? value : "";
  }

  private Endpoint getEndpoint(SpanData spanData) {
    Attributes resourceAttributes = spanData.getResource().getAttributes();

    Endpoint.Builder endpoint = Endpoint.newBuilder().ip(localAddress);

    // use the service.name from the Resource, if it's been set.
    String serviceNameValue = resourceAttributes.get(ResourceAttributes.SERVICE_NAME);
    if (serviceNameValue == null) {
      serviceNameValue = Resource.getDefault().getAttribute(ResourceAttributes.SERVICE_NAME);
    }
    // In practice should never be null unless the default Resource spec is changed.
    if (serviceNameValue != null) {
      endpoint.serviceName(serviceNameValue);
    }
    return endpoint.build();
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

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
  @Nullable
  static InetAddress produceLocalIp() {
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
      while (nics.hasMoreElements()) {
        NetworkInterface nic = nics.nextElement();
        Enumeration<InetAddress> addresses = nic.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = addresses.nextElement();
          if (address.isSiteLocalAddress()) {
            return address;
          }
        }
      }
    } catch (Exception e) {
      // don't crash the caller if there was a problem reading nics.
      logger.log(Level.FINE, "error reading nics", e);
    }
    return null;
  }
}
