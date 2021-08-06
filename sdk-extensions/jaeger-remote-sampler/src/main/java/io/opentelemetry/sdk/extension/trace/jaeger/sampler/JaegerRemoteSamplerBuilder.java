/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** A builder for {@link JaegerRemoteSampler}. */
public final class JaegerRemoteSamplerBuilder {
  private static final String DEFAULT_ENDPOINT = "localhost:14250";
  private static final int DEFAULT_POLLING_INTERVAL_MILLIS = 60000;
  private static final Sampler INITIAL_SAMPLER =
      Sampler.parentBased(Sampler.traceIdRatioBased(0.001));

  private String endpoint = DEFAULT_ENDPOINT;
  private ManagedChannel channel;
  private String serviceName;
  private Sampler initialSampler = INITIAL_SAMPLER;
  private int pollingIntervalMillis = DEFAULT_POLLING_INTERVAL_MILLIS;
  private boolean closeChannel = true;

  /**
   * Sets the service name to be used by this exporter. Required.
   *
   * @param serviceName the service name.
   * @return this.
   */
  public JaegerRemoteSamplerBuilder setServiceName(String serviceName) {
    requireNonNull(serviceName, "serviceName");
    this.serviceName = serviceName;
    return this;
  }

  /** Sets the Jaeger endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT}. */
  public JaegerRemoteSamplerBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the managed channel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   */
  public JaegerRemoteSamplerBuilder setChannel(ManagedChannel channel) {
    requireNonNull(channel, "channel");
    this.channel = channel;
    closeChannel = false;
    return this;
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_MILLIS}ms. Must be positive.
   */
  public JaegerRemoteSamplerBuilder setPollingInterval(int interval, TimeUnit unit) {
    requireNonNull(unit, "unit");
    Utils.checkArgument(interval > 0, "polling interval must be positive");
    pollingIntervalMillis = (int) unit.toMillis(interval);
    return this;
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_MILLIS}ms.
   */
  public JaegerRemoteSamplerBuilder setPollingInterval(Duration interval) {
    requireNonNull(interval, "interval");
    return setPollingInterval((int) interval.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Sets the initial sampler that is used before sampling configuration is obtained. If unset,
   * defaults to a parent-based ratio-based sampler with a ratio of 0.001.
   */
  public JaegerRemoteSamplerBuilder setInitialSampler(Sampler initialSampler) {
    requireNonNull(initialSampler, "initialSampler");
    this.initialSampler = initialSampler;
    return this;
  }

  /**
   * Builds the {@link JaegerRemoteSampler}.
   *
   * @return the remote sampler instance.
   */
  public JaegerRemoteSampler build() {
    if (channel == null) {
      channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build();
    }
    return new JaegerRemoteSampler(
        serviceName, channel, pollingIntervalMillis, initialSampler, closeChannel);
  }

  JaegerRemoteSamplerBuilder() {}
}
