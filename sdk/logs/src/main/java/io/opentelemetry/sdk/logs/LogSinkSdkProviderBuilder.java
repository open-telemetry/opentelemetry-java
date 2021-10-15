/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;

public final class LogSinkSdkProviderBuilder {

  private final List<LogProcessor> logProcessors = new ArrayList<>();
  private Resource resource = Resource.getDefault();

  LogSinkSdkProviderBuilder() {}

  /**
   * Set the resource.
   *
   * @param resource the resource
   * @return this
   */
  public LogSinkSdkProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Add a log processor.
   *
   * @param processor the log processor
   * @return this
   */
  public LogSinkSdkProviderBuilder addLogProcessor(LogProcessor processor) {
    requireNonNull(processor, "processor");
    logProcessors.add(processor);
    return this;
  }

  /**
   * Create a {@link SdkLogSinkProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public SdkLogSinkProvider build() {
    return new SdkLogSinkProvider(resource, logProcessors);
  }
}
