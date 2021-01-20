/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/** Builder class for {@link ZipkinSpanExporter}. */
public final class ZipkinSpanExporterBuilder {
  private BytesEncoder<Span> encoder = SpanBytesEncoder.JSON_V2;
  private Sender sender;
  private String serviceName =
      Resource.getDefault().getAttributes().get(ResourceAttributes.SERVICE_NAME);
  private String endpoint = ZipkinSpanExporter.DEFAULT_ENDPOINT;

  /**
   * Label of the remote node in the service graph, such as "favstar". Avoid names with variables or
   * unique identifiers embedded. Defaults to the service.name specified in the default Resource.
   *
   * <p>This is a primary label for trace lookup and aggregation, so it should be intuitive and
   * consistent. Many use a name from service discovery.
   *
   * <p>Note: this value, will be superseded by the value of {@link ResourceAttributes#SERVICE_NAME}
   * if it has been set in the {@link Resource} associated with the Tracer that created the spans.
   *
   * @param serviceName The service name. It defaults to "unknown".
   * @return this.
   * @see Resource
   * @see ResourceAttributes
   * @deprecated The default service name is now extracted from the default Resource. This method
   *     will be removed in the next release.
   */
  @Deprecated
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
