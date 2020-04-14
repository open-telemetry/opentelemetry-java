/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.zipkin;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.AttributeValue.Type;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

/**
 * This class was based on the OpenCensus zipkin exporter code at
 * https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin
 */
final class ZipkinSpanExporter implements SpanExporter {

  private static final Logger logger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  // The naming follows Zipkin convention. As an example see:
  // https://github.com/openzipkin/brave/blob/eee993f998ae57b08644cc357a6d478827428710/instrumentation/http/src/main/java/brave/http/HttpTags.java
  // Note: these 3 fields are non-private for testing
  static final String STATUS_CODE = "otel.status_code";
  static final String STATUS_DESCRIPTION = "otel.status_description";
  static final String STATUS_ERROR = "error";

  private final SpanBytesEncoder encoder;
  private final Sender sender;
  private final Endpoint localEndpoint;

  ZipkinSpanExporter(SpanBytesEncoder encoder, Sender sender, String serviceName) {
    this.encoder = encoder;
    this.sender = sender;
    this.localEndpoint = produceLocalEndpoint(serviceName);
  }

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
  static Endpoint produceLocalEndpoint(String serviceName) {
    Endpoint.Builder builder = Endpoint.newBuilder().serviceName(serviceName);
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
      if (nics == null) {
        return builder.build();
      }
      while (nics.hasMoreElements()) {
        NetworkInterface nic = nics.nextElement();
        Enumeration<InetAddress> addresses = nic.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = addresses.nextElement();
          if (address.isSiteLocalAddress()) {
            builder.ip(address);
            break;
          }
        }
      }
    } catch (Exception e) {
      // don't crash the caller if there was a problem reading nics.
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "error reading nics", e);
      }
    }
    return builder.build();
  }

  static Span generateSpan(SpanData spanData, Endpoint localEndpoint) {
    long startTimestamp = toEpochMicros(spanData.getStartEpochNanos());

    long endTimestamp = toEpochMicros(spanData.getEndEpochNanos());

    Span.Builder spanBuilder =
        Span.newBuilder()
            .traceId(spanData.getTraceId().toLowerBase16())
            .id(spanData.getSpanId().toLowerBase16())
            .kind(toSpanKind(spanData))
            .name(spanData.getName())
            .timestamp(toEpochMicros(spanData.getStartEpochNanos()))
            .duration(endTimestamp - startTimestamp)
            .localEndpoint(localEndpoint);

    if (spanData.getParentSpanId().isValid()) {
      spanBuilder.parentId(spanData.getParentSpanId().toLowerBase16());
    }

    for (Map.Entry<String, AttributeValue> label : spanData.getAttributes().entrySet()) {
      spanBuilder.putTag(label.getKey(), attributeValueToString(label.getValue()));
    }
    Status status = spanData.getStatus();
    if (status != null) {
      spanBuilder.putTag(STATUS_CODE, status.getCanonicalCode().toString());
      if (status.getDescription() != null) {
        spanBuilder.putTag(STATUS_DESCRIPTION, status.getDescription());
      }
      if (!status.isOk()) {
        spanBuilder.putTag(STATUS_ERROR, status.getCanonicalCode().toString());
      }
    }

    for (SpanData.TimedEvent annotation : spanData.getTimedEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }

    return spanBuilder.build();
  }

  @Nullable
  private static Span.Kind toSpanKind(SpanData spanData) {
    // This is a hack because the Span API did not have SpanKind.
    if (spanData.getKind() == Kind.SERVER
        || (spanData.getKind() == null && Boolean.TRUE.equals(spanData.getHasRemoteParent()))) {
      return Span.Kind.SERVER;
    }

    // This is a hack because the Span API did not have SpanKind.
    if (spanData.getKind() == Kind.CLIENT || spanData.getName().startsWith("Sent.")) {
      return Span.Kind.CLIENT;
    }

    if (spanData.getKind() == Kind.PRODUCER) {
      return Span.Kind.PRODUCER;
    }
    if (spanData.getKind() == Kind.CONSUMER) {
      return Span.Kind.CONSUMER;
    }

    return null;
  }

  private static long toEpochMicros(long epochNanos) {
    return MICROSECONDS.convert(epochNanos, NANOSECONDS);
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    Type type = attributeValue.getType();
    switch (type) {
      case STRING:
        return attributeValue.getStringValue();
      case BOOLEAN:
        return String.valueOf(attributeValue.getBooleanValue());
      case LONG:
        return String.valueOf(attributeValue.getLongValue());
      case DOUBLE:
        return String.valueOf(attributeValue.getDoubleValue());
    }
    return "";
  }

  @Override
  public ResultCode export(final Collection<SpanData> spanDataList) {
    List<byte[]> encodedSpans = new ArrayList<>(spanDataList.size());
    for (SpanData spanData : spanDataList) {
      encodedSpans.add(encoder.encode(generateSpan(spanData, localEndpoint)));
    }
    try {
      sender.sendSpans(encodedSpans).execute();
    } catch (IOException e) {
      return ResultCode.FAILED_NOT_RETRYABLE;
    }
    return ResultCode.SUCCESS;
  }

  @Override
  public void shutdown() {}

  /**
   * Create a new {@link ZipkinSpanExporter} from the given configuration.
   *
   * @param configuration a {@link ZipkinExporterConfiguration} instance.
   * @return A ready-to-use {@link ZipkinSpanExporter}
   */
  public static ZipkinSpanExporter create(ZipkinExporterConfiguration configuration) {
    return new ZipkinSpanExporter(
        configuration.getEncoder(), configuration.getSender(), configuration.getServiceName());
  }
}
