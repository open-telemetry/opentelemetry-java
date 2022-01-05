/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** A builder for {@link JaegerRemoteSampler}. */
public final class JaegerRemoteSamplerBuilder {

  private static final String GRPC_SERVICE_NAME = "jaeger.api_v2.SamplingManager";
  // Visible for testing
  static final String GRPC_ENDPOINT_PATH = "/" + GRPC_SERVICE_NAME + "/GetSamplingStrategy";

  private static final String DEFAULT_ENDPOINT_URL = "http://localhost:14250";
  private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
  private static final int DEFAULT_POLLING_INTERVAL_MILLIS = 60000;
  private static final Sampler INITIAL_SAMPLER =
      Sampler.parentBased(Sampler.traceIdRatioBased(0.001));

  @Nullable private String serviceName;
  private Sampler initialSampler = INITIAL_SAMPLER;
  private int pollingIntervalMillis = DEFAULT_POLLING_INTERVAL_MILLIS;
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  private final GrpcServiceBuilder<
          SamplingStrategyParametersMarshaller, SamplingStrategyResponseUnMarshaller>
      delegate;

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

  /**
   * Sets the Jaeger endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT_URL}.
   */
  public JaegerRemoteSamplerBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
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
   * This method is noop and deprecated. The remote sampler implementation uses a custom
   * implementation backed by Okhttp client.
   *
   * @see #setEndpoint(String)
   */
  @Deprecated
  public JaegerRemoteSamplerBuilder setChannel(ManagedChannel channel) {
    requireNonNull(channel, "channel");
    return this;
  }

  /**
   * Builds the {@link JaegerRemoteSampler}.
   *
   * @return the remote sampler instance.
   */
  public JaegerRemoteSampler build() {
    return new JaegerRemoteSampler(
        delegate.build(), serviceName, pollingIntervalMillis, initialSampler);
  }

  JaegerRemoteSamplerBuilder() {
    delegate =
        GrpcService.builder(
            "remoteSampling",
            DEFAULT_TIMEOUT_SECS,
            DEFAULT_ENDPOINT,
            () -> MarshallerRemoteSamplerServiceGrpc::newFutureStub,
            GRPC_SERVICE_NAME,
            GRPC_ENDPOINT_PATH);
  }
}
