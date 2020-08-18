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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.resources.ResourceConstants;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.attributes.SemanticAttributes;
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
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * This class was based on the OpenCensus zipkin exporter code at
 * https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin
 *
 * <p>Configuration options for {@link ZipkinSpanExporter} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link ZipkinSpanExporter}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.zipkin.service.name}: to set the service name.
 *   <li>{@code otel.zipkin.endpoint}: to set the endpoint URL.
 * </ul>
 *
 * <p>For environment variables, {@link ZipkinSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_ZIPKIN_SERVICE_NAME}: to set the service name.
 *   <li>{@code OTEL_ZIPKIN_ENDPOINT}: to set the endpoint URL.
 * </ul>
 */
public final class ZipkinSpanExporter implements SpanExporter {
  public static final String DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";
  public static final String DEFAULT_SERVICE_NAME = "unknown";

  private static final Logger logger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  // The naming follows Zipkin convention. For http see here:
  // https://github.com/openzipkin/brave/blob/eee993f998ae57b08644cc357a6d478827428710/instrumentation/http/src/main/java/brave/http/HttpTags.java
  // For discussion about GRPC errors/tags, see here:  https://github.com/openzipkin/brave/pull/999
  // Note: these 3 fields are non-private for testing
  static final String GRPC_STATUS_CODE = "grpc.status_code";
  static final String GRPC_STATUS_DESCRIPTION = "grpc.status_description";
  static final String STATUS_ERROR = "error";

  private final BytesEncoder<Span> encoder;
  private final Sender sender;
  private final Endpoint localEndpoint;

  ZipkinSpanExporter(BytesEncoder<Span> encoder, Sender sender, String serviceName) {
    this.encoder = encoder;
    this.sender = sender;
    this.localEndpoint = produceLocalEndpoint(serviceName);
  }

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
  static Endpoint produceLocalEndpoint(String serviceName) {
    Endpoint.Builder builder = Endpoint.newBuilder().serviceName(serviceName);
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
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
    Endpoint endpoint = chooseEndpoint(spanData, localEndpoint);

    long startTimestamp = toEpochMicros(spanData.getStartEpochNanos());

    long endTimestamp = toEpochMicros(spanData.getEndEpochNanos());

    final Span.Builder spanBuilder =
        Span.newBuilder()
            .traceId(spanData.getTraceId().toLowerBase16())
            .id(spanData.getSpanId().toLowerBase16())
            .kind(toSpanKind(spanData))
            .name(spanData.getName())
            .timestamp(toEpochMicros(spanData.getStartEpochNanos()))
            .duration(endTimestamp - startTimestamp)
            .localEndpoint(endpoint);

    if (spanData.getParentSpanId().isValid()) {
      spanBuilder.parentId(spanData.getParentSpanId().toLowerBase16());
    }

    ReadableAttributes spanAttributes = spanData.getAttributes();
    spanAttributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            spanBuilder.putTag(key, attributeValueToString(value));
          }
        });
    Status status = spanData.getStatus();
    // for GRPC spans, include status code & description.
    if (status != null && spanAttributes.get(SemanticAttributes.RPC_SERVICE.key()) != null) {
      spanBuilder.putTag(GRPC_STATUS_CODE, status.getCanonicalCode().toString());
      if (status.getDescription() != null) {
        spanBuilder.putTag(GRPC_STATUS_DESCRIPTION, status.getDescription());
      }
    }
    // add the error tag, if it isn't already in the source span.
    if (status != null && !status.isOk() && spanAttributes.get(STATUS_ERROR) == null) {
      spanBuilder.putTag(STATUS_ERROR, status.getCanonicalCode().toString());
    }

    for (Event annotation : spanData.getEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }

    return spanBuilder.build();
  }

  private static Endpoint chooseEndpoint(SpanData spanData, Endpoint localEndpoint) {
    ReadableAttributes resourceAttributes = spanData.getResource().getAttributes();

    // use the service.name from the Resource, if it's been set.
    AttributeValue serviceNameValue = resourceAttributes.get(ResourceConstants.SERVICE_NAME);
    if (serviceNameValue == null) {
      return localEndpoint;
    }
    return Endpoint.newBuilder().serviceName(serviceNameValue.getStringValue()).build();
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
    return NANOSECONDS.toMicros(epochNanos);
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    AttributeValue.Type type = attributeValue.getType();
    switch (type) {
      case STRING:
        return attributeValue.getStringValue();
      case BOOLEAN:
        return String.valueOf(attributeValue.getBooleanValue());
      case LONG:
        return String.valueOf(attributeValue.getLongValue());
      case DOUBLE:
        return String.valueOf(attributeValue.getDoubleValue());
      case STRING_ARRAY:
        return commaSeparated(attributeValue.getStringArrayValue());
      case BOOLEAN_ARRAY:
        return commaSeparated(attributeValue.getBooleanArrayValue());
      case LONG_ARRAY:
        return commaSeparated(attributeValue.getLongArrayValue());
      case DOUBLE_ARRAY:
        return commaSeparated(attributeValue.getDoubleArrayValue());
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
      encodedSpans.add(encoder.encode(generateSpan(spanData, localEndpoint)));
    }
    try {
      sender.sendSpans(encodedSpans).execute();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to export spans", e);
      return CompletableResultCode.ofFailure();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    // nothing required here
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public void shutdown() {
    try {
      sender.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Exception while closing the Zipkin Sender instance", e);
    }
  }

  /**
   * Returns a new Builder for {@link ZipkinSpanExporter}.
   *
   * @return a new {@link ZipkinSpanExporter}.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder class for {@link ZipkinSpanExporter}. */
  public static final class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SERVICE_NAME = "otel.zipkin.service.name";
    private static final String KEY_ENDPOINT = "otel.zipkin.endpoint";
    private BytesEncoder<Span> encoder = SpanBytesEncoder.JSON_V2;
    private Sender sender;
    private String serviceName = DEFAULT_SERVICE_NAME;
    private String endpoint = DEFAULT_ENDPOINT;

    /**
     * Label of the remote node in the service graph, such as "favstar". Avoid names with variables
     * or unique identifiers embedded. Defaults to "unknown".
     *
     * <p>This is a primary label for trace lookup and aggregation, so it should be intuitive and
     * consistent. Many use a name from service discovery.
     *
     * <p>Note: this value, will be superseded by the value of {@link
     * io.opentelemetry.sdk.resources.ResourceConstants#SERVICE_NAME} if it has been set in the
     * {@link io.opentelemetry.sdk.resources.Resource} associated with the Tracer that created the
     * spans.
     *
     * <p>This property is required to be set.
     *
     * @param serviceName The service name. It defaults to "unknown".
     * @return this.
     * @see io.opentelemetry.sdk.resources.Resource
     * @see io.opentelemetry.sdk.resources.ResourceConstants
     * @since 0.4.0
     */
    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Sets the Zipkin sender. Implements the client side of the span transport. A {@link
     * OkHttpSender} is a good default.
     *
     * <p>The {@link Sender#close()} method will be called when the exporter is shut down.
     *
     * @param sender the Zipkin sender implementation.
     * @return this.
     * @since 0.4.0
     */
    public Builder setSender(Sender sender) {
      this.sender = sender;
      return this;
    }

    /**
     * Sets the {@link BytesEncoder}, which controls the format used by the {@link Sender}. Defaults
     * to the {@link SpanBytesEncoder#JSON_V2}.
     *
     * @param encoder the {@code BytesEncoder} to use.
     * @return this.
     * @see SpanBytesEncoder
     * @since 0.4.0
     */
    public Builder setEncoder(BytesEncoder<Span> encoder) {
      this.encoder = encoder;
      return this;
    }

    /**
     * Sets the zipkin endpoint. This will use the endpoint to assign a {@link OkHttpSender}
     * instance to this builder.
     *
     * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
     * @return this.
     * @see OkHttpSender
     * @since 0.4.0
     */
    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     */
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      String stringValue = getStringProperty(KEY_SERVICE_NAME, configMap);
      if (stringValue != null) {
        this.setServiceName(stringValue);
      }
      stringValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (stringValue != null) {
        this.setEndpoint(stringValue);
      }
      return this;
    }

    /**
     * Builds a {@link ZipkinSpanExporter}.
     *
     * @return a {@code ZipkinSpanExporter}.
     * @since 0.4.0
     */
    public ZipkinSpanExporter build() {
      if (sender == null) {
        sender = OkHttpSender.create(endpoint);
      }
      return new ZipkinSpanExporter(this.encoder, this.sender, this.serviceName);
    }
  }
}
