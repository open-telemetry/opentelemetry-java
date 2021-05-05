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
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import zipkin2.Callback;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.reporter.Sender;

/**
 * This class was based on the OpenCensus zipkin exporter code at
 * https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin
 */
public final class ZipkinSpanExporter implements SpanExporter {
  public static final String DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";
  public static final Logger baseLogger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(baseLogger);

  static final String OTEL_DROPPED_ATTRIBUTES_COUNT = "otel.dropped_attributes_count";
  static final String OTEL_DROPPED_EVENTS_COUNT = "otel.dropped_events_count";
  static final String OTEL_STATUS_CODE = "otel.status_code";
  static final AttributeKey<String> STATUS_ERROR = stringKey("error");

  static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
  static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";

  private final BytesEncoder<Span> encoder;
  private final Sender sender;
  @Nullable private final InetAddress localAddress;

  ZipkinSpanExporter(BytesEncoder<Span> encoder, Sender sender) {
    this.encoder = encoder;
    this.sender = sender;
    localAddress = produceLocalIp();
  }

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
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
      baseLogger.log(Level.FINE, "error reading nics", e);
    }
    return null;
  }

  Span generateSpan(SpanData spanData) {
    Endpoint endpoint = getEndpoint(spanData);

    long startTimestamp = toEpochMicros(spanData.getStartEpochNanos());
    long endTimestamp = toEpochMicros(spanData.getEndEpochNanos());

    final Span.Builder spanBuilder =
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
    int numberOfAttributes = spanAttributes.size();
    if (numberOfAttributes != spanData.getTotalAttributeCount()) {
      spanBuilder.putTag(
          OTEL_DROPPED_ATTRIBUTES_COUNT,
          String.valueOf(spanData.getTotalAttributeCount() - numberOfAttributes));
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

    InstrumentationLibraryInfo instrumentationLibraryInfo =
        spanData.getInstrumentationLibraryInfo();

    if (!instrumentationLibraryInfo.getName().isEmpty()) {
      spanBuilder.putTag(KEY_INSTRUMENTATION_LIBRARY_NAME, instrumentationLibraryInfo.getName());
    }
    if (instrumentationLibraryInfo.getVersion() != null) {
      spanBuilder.putTag(
          KEY_INSTRUMENTATION_LIBRARY_VERSION, instrumentationLibraryInfo.getVersion());
    }
    for (EventData annotation : spanData.getEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }
    if (spanData.getEvents().size() != spanData.getTotalRecordedEvents()) {
      spanBuilder.putTag(
          OTEL_DROPPED_EVENTS_COUNT,
          String.valueOf(spanData.getTotalRecordedEvents() - spanData.getEvents().size()));
    }

    return spanBuilder.build();
  }

  private static String nullToEmpty(String value) {
    return value != null ? value : "";
  }

  private Endpoint getEndpoint(SpanData spanData) {
    Attributes resourceAttributes = spanData.getResource().getAttributes();

    // use the service.name from the Resource, if it's been set.
    String serviceNameValue = resourceAttributes.get(ResourceAttributes.SERVICE_NAME);
    if (serviceNameValue == null) {
      serviceNameValue = Resource.getDefault().getAttributes().get(ResourceAttributes.SERVICE_NAME);
    }
    return Endpoint.newBuilder().serviceName(serviceNameValue).ip(localAddress).build();
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

  @Override
  public CompletableResultCode export(final Collection<SpanData> spanDataList) {
    List<byte[]> encodedSpans = new ArrayList<>(spanDataList.size());
    for (SpanData spanData : spanDataList) {
      encodedSpans.add(encoder.encode(generateSpan(spanData)));
    }

    final CompletableResultCode result = new CompletableResultCode();
    sender
        .sendSpans(encodedSpans)
        .enqueue(
            new Callback<Void>() {
              @Override
              public void onSuccess(Void value) {
                result.succeed();
              }

              @Override
              public void onError(Throwable t) {
                logger.log(Level.WARNING, "Failed to export spans", t);
                result.fail();
              }
            });
    return result;
  }

  @Override
  public CompletableResultCode flush() {
    // nothing required here
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    try {
      sender.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Exception while closing the Zipkin Sender instance", e);
    }
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new Builder for {@link ZipkinSpanExporter}.
   *
   * @return a new {@link ZipkinSpanExporter}.
   */
  public static ZipkinSpanExporterBuilder builder() {
    return new ZipkinSpanExporterBuilder();
  }

  // VisibleForTesting
  InetAddress getLocalAddressForTest() {
    return localAddress;
  }
}
