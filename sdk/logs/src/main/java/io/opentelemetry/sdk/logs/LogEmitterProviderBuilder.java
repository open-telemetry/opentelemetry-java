/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;

/** Builder class for {@link LogEmitterProvider} instances. */
public final class LogEmitterProviderBuilder {

  private final List<LogProcessor> logProcessors = new ArrayList<>();
  private Resource resource = Resource.getDefault();

  LogEmitterProviderBuilder() {}

  /**
   * Assign a {@link Resource} to be attached to all {@link LogData} created by {@link LogEmitter}s
   * obtained from the {@link LogEmitterProvider}.
   *
   * @param resource the resource
   * @return this
   */
  public LogEmitterProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Add a log processor. {@link LogProcessor#emit(LogData)} will be called each time {@link
   * LogEmitter#emit(LogRecord)} is called for emitter instances obtained from the {@link
   * LogEmitterProvider}.
   *
   * @param processor the log processor
   * @return this
   */
  public LogEmitterProviderBuilder addLogProcessor(LogProcessor processor) {
    requireNonNull(processor, "processor");
    logProcessors.add(processor);
    return this;
  }

  /**
   * Create a {@link LogEmitterProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public LogEmitterProvider build() {
    return new LogEmitterProvider(resource, logProcessors);
  }
}
