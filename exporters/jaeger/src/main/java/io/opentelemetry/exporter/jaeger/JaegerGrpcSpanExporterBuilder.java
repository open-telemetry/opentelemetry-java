/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Builder utility for this exporter. */
public final class JaegerGrpcSpanExporterBuilder {
  private static final String DEFAULT_ENDPOINT = "localhost:14250";
  private static final String DEFAULT_SERVICE_NAME = "unknown";
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  private String serviceName = DEFAULT_SERVICE_NAME;
  private String endpoint = DEFAULT_ENDPOINT;
  private ManagedChannel channel;
  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);

  /**
   * Sets the service name to be used by this exporter, if none is found in the Resource associated
   * with a span.
   *
   * @param serviceName the service name.
   * @return this.
   * @deprecated The default service name is now extracted from the default Resource. This method
   *     will be removed in the next release.
   */
  @Deprecated
  public JaegerGrpcSpanExporterBuilder setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  /**
   * Sets the managed channel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   *
   * @param channel the channel to use.
   * @return this.
   */
  public JaegerGrpcSpanExporterBuilder setChannel(ManagedChannel channel) {
    this.channel = channel;
    return this;
  }

  /**
   * Sets the Jaeger endpoint to connect to. Optional, defaults to "localhost:14250".
   *
   * @param endpoint The Jaeger endpoint URL, ex. "jaegerhost:14250".
   * @return this.
   */
  public JaegerGrpcSpanExporterBuilder setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public JaegerGrpcSpanExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public JaegerGrpcSpanExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance.
   */
  public JaegerGrpcSpanExporter build() {
    if (channel == null) {
      channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build();
    }
    return new JaegerGrpcSpanExporter(serviceName, channel, timeoutNanos);
  }

  JaegerGrpcSpanExporterBuilder() {}
}
