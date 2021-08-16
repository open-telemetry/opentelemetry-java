/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import javax.annotation.Nullable;
import org.apache.thrift.transport.TTransportException;

/** Builder utility for this exporter. */
public final class JaegerThriftSpanExporterBuilder {

  private String endpoint = JaegerThriftSpanExporter.DEFAULT_ENDPOINT;
  @Nullable private ThriftSender thriftSender;

  /**
   * Explicitly set the {@link ThriftSender} instance to use for this Exporter. Will override any
   * endpoint that has been set.
   *
   * @param thriftSender The ThriftSender to use.
   * @return this.
   */
  public JaegerThriftSpanExporterBuilder setThriftSender(ThriftSender thriftSender) {
    this.thriftSender = thriftSender;
    return this;
  }

  /**
   * Sets the Jaeger endpoint to connect to. Needs to include the full API path for trace ingest.
   *
   * <p>Optional, defaults to "http://localhost:14268/api/traces".
   *
   * @param endpoint The Jaeger endpoint URL, ex. "https://jaegerhost:14268/api/traces".
   * @return this.
   */
  public JaegerThriftSpanExporterBuilder setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance.
   */
  public JaegerThriftSpanExporter build() {
    ThriftSender thriftSender = this.thriftSender;
    if (thriftSender == null) {
      try {
        thriftSender = new HttpSender.Builder(endpoint).build();
      } catch (TTransportException e) {
        throw new IllegalStateException("Failed to construct a thrift HttpSender.", e);
      }
    }
    return new JaegerThriftSpanExporter(thriftSender);
  }

  JaegerThriftSpanExporterBuilder() {}
}
