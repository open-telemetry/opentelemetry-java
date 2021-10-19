/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.logs.data.LogRecord;
import java.util.ArrayList;
import java.util.List;

public final class LogSinkSdkProviderBuilder {

  private final List<LogProcessor> logProcessors = new ArrayList<>();

  LogSinkSdkProviderBuilder() {}

  /**
   * Add a LogProcessor to the log pipeline that will be built. {@link LogProcessor} will be called
   * each time a {@link LogRecord} is offered to a {@link LogSink}.
   *
   * @param processor the processor to be added to the processing pipeline.
   * @return this
   */
  public LogSinkSdkProviderBuilder addLogProcessor(LogProcessor processor) {
    requireNonNull(processor, "processor can not be null");
    logProcessors.add(processor);
    return this;
  }

  public LogSinkSdkProvider build() {
    return new LogSinkSdkProvider(logProcessors);
  }
}
