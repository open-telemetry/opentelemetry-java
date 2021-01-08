/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import java.util.Map;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/** Builder class for {@link ZipkinSpanExporter}. */
@SuppressWarnings("deprecation") // Remove after ConfigBuilder is deleted
public final class ZipkinSpanExporterBuilder
    extends io.opentelemetry.sdk.common.export.ConfigBuilder<ZipkinSpanExporterBuilder> {
  private static final String KEY_SERVICE_NAME = "otel.exporter.zipkin.service.name";
  private static final String KEY_ENDPOINT = "otel.exporter.zipkin.endpoint";
  private BytesEncoder<Span> encoder = SpanBytesEncoder.JSON_V2;
  private Sender sender;
  private String serviceName = ZipkinSpanExporter.DEFAULT_SERVICE_NAME;
  private String endpoint = ZipkinSpanExporter.DEFAULT_ENDPOINT;

  /**
   * Label of the remote node in the service graph, such as "favstar". Avoid names with variables or
   * unique identifiers embedded. Defaults to "unknown".
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
  public ZipkinSpanExporterBuilder setServiceName(String serviceName) {
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
  public ZipkinSpanExporterBuilder setSender(Sender sender) {
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
  public ZipkinSpanExporterBuilder setEncoder(BytesEncoder<Span> encoder) {
    this.encoder = encoder;
    return this;
  }

  /**
   * Sets the zipkin endpoint. This will use the endpoint to assign a {@link OkHttpSender} instance
   * to this builder.
   *
   * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
   * @return this.
   * @see OkHttpSender
   */
  public ZipkinSpanExporterBuilder setEndpoint(String endpoint) {
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
  protected ZipkinSpanExporterBuilder fromConfigMap(
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
