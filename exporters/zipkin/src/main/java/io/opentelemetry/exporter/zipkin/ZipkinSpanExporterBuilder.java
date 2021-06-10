/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/** Builder class for {@link ZipkinSpanExporter}. */
public final class ZipkinSpanExporterBuilder {
  private BytesEncoder<Span> encoder = SpanBytesEncoder.JSON_V2;
  private Sender sender;
  private String endpoint = ZipkinSpanExporter.DEFAULT_ENDPOINT;
  private long readTimeoutMillis = TimeUnit.SECONDS.toMillis(10);

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
    requireNonNull(sender, "sender");
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
    requireNonNull(encoder, "encoder");
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
    requireNonNull(endpoint, "endpoint");
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the maximum time to wait for the export of a batch of spans. If unset, defaults to 10s.
   *
   * @return this.
   * @since 1.2.0
   */
  public ZipkinSpanExporterBuilder setReadTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    this.readTimeoutMillis = unit.toMillis(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for the export of a batch of spans. If unset, defaults to 10s.
   *
   * @return this.
   * @since 1.2.0
   */
  public ZipkinSpanExporterBuilder setReadTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    setReadTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    return this;
  }

  /**
   * Builds a {@link ZipkinSpanExporter}.
   *
   * @return a {@code ZipkinSpanExporter}.
   */
  public ZipkinSpanExporter build() {
    if (sender == null) {
      sender =
          OkHttpSender.newBuilder().endpoint(endpoint).readTimeout((int) readTimeoutMillis).build();
    }
    return new ZipkinSpanExporter(this.encoder, this.sender);
  }
}
