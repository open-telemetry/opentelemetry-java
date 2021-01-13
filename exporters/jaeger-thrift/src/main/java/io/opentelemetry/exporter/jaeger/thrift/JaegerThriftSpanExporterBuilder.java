/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thrift.internal.senders.ThriftSender;

/** Builder utility for this exporter. */
public final class JaegerThriftSpanExporterBuilder {

  private String serviceName = JaegerThriftSpanExporter.DEFAULT_SERVICE_NAME;
  private String endpoint = JaegerThriftSpanExporter.DEFAULT_ENDPOINT;
  private ThriftSender thriftSender;

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
   * Sets the service name to be used by this exporter. Required.
   *
   * @param serviceName the service name.
   * @return this.
   */
  public JaegerThriftSpanExporterBuilder setServiceName(String serviceName) {
    this.serviceName = serviceName;
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
    if (thriftSender == null) {
      thriftSender = new HttpSender.Builder(endpoint).build();
    }
    return new JaegerThriftSpanExporter(thriftSender, serviceName);
  }

  JaegerThriftSpanExporterBuilder() {}
}
