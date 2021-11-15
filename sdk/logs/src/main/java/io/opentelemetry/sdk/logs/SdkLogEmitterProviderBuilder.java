/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.logs.LogEmitter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;

/** Builder class for {@link SdkLogEmitterProvider} instances. */
public final class SdkLogEmitterProviderBuilder {

  private final List<LogProcessor> logProcessors = new ArrayList<>();
  private Resource resource = Resource.getDefault();
  private Clock clock = Clock.getDefault();

  SdkLogEmitterProviderBuilder() {}

  /**
   * Assign a {@link Resource} to be attached to all {@link LogData} created by {@link LogEmitter}s
   * obtained from the {@link SdkLogEmitterProvider}.
   *
   * @param resource the resource
   * @return this
   */
  public SdkLogEmitterProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Add a log processor. {@link LogProcessor#emit(LogData)} will be called each time a log is
   * emitted by {@link LogEmitter} instances obtained from the {@link SdkLogEmitterProvider}.
   *
   * @param processor the log processor
   * @return this
   */
  public SdkLogEmitterProviderBuilder addLogProcessor(LogProcessor processor) {
    requireNonNull(processor, "processor");
    logProcessors.add(processor);
    return this;
  }

  /**
   * Assign a {@link Clock}. The {@link Clock} may be used to determine "now" in the event that the
   * epoch millis are not set directly.
   *
   * <p>The {@code clock} must be thread-safe and return immediately (no remote calls, as contention
   * free as possible).
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkLogEmitterProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Create a {@link SdkLogEmitterProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public SdkLogEmitterProvider build() {
    return new SdkLogEmitterProvider(resource, logProcessors, clock);
  }
}
