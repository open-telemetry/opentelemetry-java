/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** A builder for {@link AwsXrayRemoteSampler}. */
public final class AwsXrayRemoteSamplerBuilder {

  private static final String DEFAULT_ENDPOINT = "http://localhost:2000";
  private static final long DEFAULT_POLLING_INTERVAL_SECS = 300;

  private final Resource resource;

  private String endpoint = DEFAULT_ENDPOINT;
  private Sampler initialSampler = Sampler.parentBased(Sampler.alwaysOn());
  private long pollingIntervalNanos = TimeUnit.SECONDS.toNanos(DEFAULT_POLLING_INTERVAL_SECS);

  AwsXrayRemoteSamplerBuilder(Resource resource) {
    this.resource = resource;
  }

  /**
   * Sets the endpoint for the TCP proxy to connect to. This is the address to the port on the
   * OpenTelemetry Collector configured for proxying X-Ray sampling requests. If unset, defaults to
   * {@value DEFAULT_ENDPOINT}.
   */
  public AwsXrayRemoteSamplerBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_SECS}s. Must be positive.
   */
  public AwsXrayRemoteSamplerBuilder setPollingInterval(Duration delay) {
    requireNonNull(delay, "delay");
    return setPollingInterval(delay.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_SECS}s. Must be positive.
   */
  public AwsXrayRemoteSamplerBuilder setPollingInterval(long delay, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(delay >= 0, "delay must be non-negative");
    pollingIntervalNanos = unit.toNanos(delay);
    return this;
  }

  /**
   * Sets the initial sampler that is used before sampling configuration is obtained. If unset,
   * defaults to a parent-based always-on sampler.
   */
  public AwsXrayRemoteSamplerBuilder setInitialSampler(Sampler initialSampler) {
    requireNonNull(initialSampler, "initialSampler");
    this.initialSampler = initialSampler;
    return this;
  }

  /** Returns a {@link AwsXrayRemoteSampler} with the configuration of this builder. */
  public AwsXrayRemoteSampler build() {
    return new AwsXrayRemoteSampler(resource, endpoint, initialSampler, pollingIntervalNanos);
  }
}
