/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class LoggerProviderFactory implements Factory<LoggerProvider, SdkLoggerProviderBuilder> {

  private static final LoggerProviderFactory INSTANCE = new LoggerProviderFactory();

  private LoggerProviderFactory() {}

  static LoggerProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkLoggerProviderBuilder create(
      @Nullable LoggerProvider model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return SdkLoggerProvider.builder();
    }

    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();

    LogLimits logLimits =
        LogLimitsFactory.getInstance().create(model.getLimits(), spiHelper, closeables);
    builder.setLogLimits(() -> logLimits);

    List<LogRecordProcessor> processors = model.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addLogRecordProcessor(
                  LogRecordProcessorFactory.getInstance()
                      .create(processor, spiHelper, closeables)));
    }

    return builder;
  }
}
