/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.zipkin;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.export.SpanExporter;
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
import zipkin2.Callback;
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
 *   <li>{@code otel.exporter.zipkin.service.name}: to set the service name.
 *   <li>{@code otel.exporter.zipkin.endpoint}: to set the endpoint URL.
 * </ul>
 *
 * <p>For environment variables, {@link ZipkinSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_EXPORTER_ZIPKIN_ENDPOINT}: to set the service name.
 *   <li>{@code OTEL_EXPORTER_ZIPKIN_ENDPOINT}: to set the endpoint URL.
 * </ul>
 */
public final class ZipkinSpanExporter implements SpanExporter {
  public static final String DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";
  public static final String DEFAULT_SERVICE_NAME = "unknown";

  private static final Logger logger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  static final String OTEL_STATUS_CODE = "otel.status_code";
  static final String OTEL_STATUS_DESCRIPTION = "otel.status_description";
  static final AttributeKey<String> STATUS_ERROR = stringKey("error");

  static final String KEY_INSTRUMENTATION_LIBRARY_NAME = "otel.library.name";
  static final String KEY_INSTRUMENTATION_LIBRARY_VERSION = "otel.library.version";

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
            .traceId(spanData.getTraceId())
            .id(spanData.getSpanId())
            .kind(toSpanKind(spanData))
            .name(spanData.getName())
            .timestamp(toEpochMicros(spanData.getStartEpochNanos()))
            .duration(Math.max(1, endTimestamp - startTimestamp))
            .localEndpoint(endpoint);

    if (SpanId.isValid(spanData.getParentSpanId())) {
      spanBuilder.parentId(spanData.getParentSpanId());
    }

    ReadableAttributes spanAttributes = spanData.getAttributes();
    spanAttributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void consume(AttributeKey<T> key, T value) {
            spanBuilder.putTag(key.getKey(), valueToString(key, value));
          }
        });
    SpanData.Status status = spanData.getStatus();
    // for GRPC spans, include status code & description.
    if (status != null && spanAttributes.get(SemanticAttributes.RPC_SERVICE) != null) {
      spanBuilder.putTag(OTEL_STATUS_CODE, status.getCanonicalCode().toString());
      if (status.getDescription() != null) {
        spanBuilder.putTag(OTEL_STATUS_DESCRIPTION, status.getDescription());
      }
    }
    // add the error tag, if it isn't already in the source span.
    if (status != null && !status.isOk() && spanAttributes.get(STATUS_ERROR) == null) {
      spanBuilder.putTag(STATUS_ERROR.getKey(), status.getCanonicalCode().toString());
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

    for (Event annotation : spanData.getEvents()) {
      spanBuilder.addAnnotation(toEpochMicros(annotation.getEpochNanos()), annotation.getName());
    }

    return spanBuilder.build();
  }

  private static Endpoint chooseEndpoint(SpanData spanData, Endpoint localEndpoint) {
    ReadableAttributes resourceAttributes = spanData.getResource().getAttributes();

    // use the service.name from the Resource, if it's been set.
    String serviceNameValue = resourceAttributes.get(ResourceAttributes.SERVICE_NAME);
    if (serviceNameValue == null) {
      return localEndpoint;
    }
    return Endpoint.newBuilder().serviceName(serviceNameValue).build();
  }

  @Nullable
  private static Span.Kind toSpanKind(SpanData spanData) {
    // This is a hack because the Span API did not have SpanKind.
    if (spanData.getKind() == Kind.SERVER
        || (spanData.getKind() == null && Boolean.TRUE.equals(spanData.hasRemoteParent()))) {
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

  private static <T> String valueToString(AttributeKey<T> key, T attributeValue) {
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
      encodedSpans.add(encoder.encode(generateSpan(spanData, localEndpoint)));
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
  public static Builder builder() {
    return new Builder();
  }

  /** Builder class for {@link ZipkinSpanExporter}. */
  public static final class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SERVICE_NAME = "otel.exporter.zipkin.service.name";
    private static final String KEY_ENDPOINT = "otel.exporter.zipkin.endpoint";
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
     * io.opentelemetry.sdk.resources.ResourceAttributes#SERVICE_NAME} if it has been set in the
     * {@link io.opentelemetry.sdk.resources.Resource} associated with the Tracer that created the
     * spans.
     *
     * <p>This property is required to be set.
     *
     * @param serviceName The service name. It defaults to "unknown".
     * @return this.
     * @see io.opentelemetry.sdk.resources.Resource
     * @see io.opentelemetry.sdk.resources.ResourceAttributes
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
     */
    public ZipkinSpanExporter build() {
      if (sender == null) {
        sender = OkHttpSender.create(endpoint);
      }
      return new ZipkinSpanExporter(this.encoder, this.sender, this.serviceName);
    }
  }
}
