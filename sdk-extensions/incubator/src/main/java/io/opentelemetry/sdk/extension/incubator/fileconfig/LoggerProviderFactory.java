/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import java.io.Closeable;
import java.util.List;

final class LoggerProviderFactory
    implements Factory<LoggerProviderAndAttributeLimits, SdkLoggerProviderBuilder> {

  private static final LoggerProviderFactory INSTANCE = new LoggerProviderFactory();

  private LoggerProviderFactory() {}

  static LoggerProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkLoggerProviderBuilder create(
      LoggerProviderAndAttributeLimits model, SpiHelper spiHelper, List<Closeable> closeables) {
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();

    LoggerProviderModel loggerProviderModel = model.getLoggerProvider();
    if (loggerProviderModel == null) {
      return builder;
    }

    LogLimits logLimits =
        LogLimitsFactory.getInstance()
            .create(
                LogRecordLimitsAndAttributeLimits.create(
                    model.getAttributeLimits(), loggerProviderModel.getLimits()),
                spiHelper,
                closeables);
    builder.setLogLimits(() -> logLimits);

    List<LogRecordProcessorModel> processors = loggerProviderModel.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addLogRecordProcessor(
                  LogRecordProcessorFactory.getInstance()
                      .create(processor, spiHelper, closeables)));
    }

    ExperimentalLoggerConfiguratorModel loggerConfiguratorModel =
        loggerProviderModel.getLoggerConfiguratorDevelopment();
    if (loggerConfiguratorModel != null) {
      ExperimentalLoggerConfigModel defaultConfigModel = loggerConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<LoggerConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            LoggerConfigFactory.INSTANCE.create(defaultConfigModel, spiHelper, closeables));
      }
      List<ExperimentalLoggerMatcherAndConfigModel> loggerMatcherAndConfigs =
          loggerConfiguratorModel.getLoggers();
      if (loggerMatcherAndConfigs != null) {
        for (ExperimentalLoggerMatcherAndConfigModel loggerMatcherAndConfig :
            loggerMatcherAndConfigs) {
          String name = requireNonNull(loggerMatcherAndConfig.getName(), "logger matcher name");
          ExperimentalLoggerConfigModel config = loggerMatcherAndConfig.getConfig();
          if (name == null || config == null) {
            continue;
          }
          configuratorBuilder.addCondition(
              ScopeConfiguratorBuilder.nameMatchesGlob(name),
              LoggerProviderFactory.LoggerConfigFactory.INSTANCE.create(
                  config, spiHelper, closeables));
        }
      }
      SdkLoggerProviderUtil.setLoggerConfigurator(builder, configuratorBuilder.build());
    }

    return builder;
  }

  private static class LoggerConfigFactory
      implements Factory<ExperimentalLoggerConfigModel, LoggerConfig> {

    private static final LoggerProviderFactory.LoggerConfigFactory INSTANCE =
        new LoggerProviderFactory.LoggerConfigFactory();

    @Override
    public LoggerConfig create(
        ExperimentalLoggerConfigModel model, SpiHelper spiHelper, List<Closeable> closeables) {
      if (model.getDisabled() != null && model.getDisabled()) {
        return LoggerConfig.disabled();
      }
      return LoggerConfig.defaultConfig();
    }
  }
}
