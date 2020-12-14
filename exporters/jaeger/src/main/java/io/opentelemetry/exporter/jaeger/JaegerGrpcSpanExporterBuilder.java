/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Map;

/** Builder utility for this exporter. */
public class JaegerGrpcSpanExporterBuilder extends ConfigBuilder<JaegerGrpcSpanExporterBuilder> {
  private static final String KEY_SERVICE_NAME = "otel.exporter.jaeger.service.name";
  private static final String KEY_ENDPOINT = "otel.exporter.jaeger.endpoint";

  private String serviceName = JaegerGrpcSpanExporter.DEFAULT_SERVICE_NAME;
  private String endpoint = JaegerGrpcSpanExporter.DEFAULT_ENDPOINT;
  private ManagedChannel channel;
  private long deadlineMs = JaegerGrpcSpanExporter.DEFAULT_DEADLINE_MS; // 10 seconds

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
   * Sets the max waiting time for the collector to process each span batch. Optional.
   *
   * @param deadlineMs the max waiting time in millis.
   * @return this.
   */
  public JaegerGrpcSpanExporterBuilder setDeadlineMs(long deadlineMs) {
    this.deadlineMs = deadlineMs;
    return this;
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
    return new JaegerGrpcSpanExporter(serviceName, channel, deadlineMs);
  }

  JaegerGrpcSpanExporterBuilder() {}
}
