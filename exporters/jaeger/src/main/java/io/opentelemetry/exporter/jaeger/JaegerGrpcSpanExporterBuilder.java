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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Builder utility for this exporter. */
@SuppressWarnings("deprecation") // Remove after ConfigBuilder is deleted
public final class JaegerGrpcSpanExporterBuilder
    extends io.opentelemetry.sdk.common.export.ConfigBuilder<JaegerGrpcSpanExporterBuilder> {
  private static final String KEY_SERVICE_NAME = "otel.exporter.jaeger.service.name";
  private static final String KEY_ENDPOINT = "otel.exporter.jaeger.endpoint";

  private static final String DEFAULT_ENDPOINT = "localhost:14250";
  private static final String DEFAULT_SERVICE_NAME = "unknown";
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  private String serviceName = DEFAULT_SERVICE_NAME;
  private String endpoint = DEFAULT_ENDPOINT;
  private ManagedChannel channel;
  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);

  /**
   * Sets the service name to be used by this exporter. Required.
   *
   * @param serviceName the service name.
   * @return this.
   */
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
   * Sets the max waiting time for the collector to process each span batch. Optional.
   *
   * @param deadlineMs the max waiting time in millis.
   * @return this.
   * @deprecated Use {@link #setTimeout(long, TimeUnit)}
   */
  @Deprecated
  public JaegerGrpcSpanExporterBuilder setDeadlineMs(long deadlineMs) {
    return setTimeout(Duration.ofMillis(deadlineMs));
  }

  /**
   * Sets the configuration values from the given configuration map for only the available keys.
   *
   * @param configMap {@link Map} holding the configuration values.
   * @return this.
   */
  @Override
  protected JaegerGrpcSpanExporterBuilder fromConfigMap(
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
