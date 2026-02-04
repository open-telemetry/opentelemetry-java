/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel.SeverityNumber;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.LoggerConfigBuilder;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
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
      LoggerProviderAndAttributeLimits model, DeclarativeConfigContext context) {
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();

    LoggerProviderModel loggerProviderModel = model.getLoggerProvider();
    if (loggerProviderModel == null) {
      return builder;
    }

    MeterProvider meterProvider = context.getMeterProvider();
    if (meterProvider != null) {
      builder.setMeterProvider(() -> meterProvider);
    }

    LogLimits logLimits =
        LogLimitsFactory.getInstance()
            .create(
                LogRecordLimitsAndAttributeLimits.create(
                    model.getAttributeLimits(), loggerProviderModel.getLimits()),
                context);
    builder.setLogLimits(() -> logLimits);

    List<LogRecordProcessorModel> processors = loggerProviderModel.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addLogRecordProcessor(
                  LogRecordProcessorFactory.getInstance().create(processor, context)));
    }

    ExperimentalLoggerConfiguratorModel loggerConfiguratorModel =
        loggerProviderModel.getLoggerConfiguratorDevelopment();
    if (loggerConfiguratorModel != null) {
      ExperimentalLoggerConfigModel defaultConfigModel = loggerConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<LoggerConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            LoggerConfigFactory.INSTANCE.create(defaultConfigModel, context));
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
              LoggerProviderFactory.LoggerConfigFactory.INSTANCE.create(config, context));
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
        ExperimentalLoggerConfigModel model, DeclarativeConfigContext context) {
      LoggerConfigBuilder configBuilder = LoggerConfig.builder();
      if (model.getDisabled() != null && model.getDisabled()) {
        configBuilder.setEnabled(false);
      }
      if (model.getTraceBased() != null && model.getTraceBased()) {
        configBuilder.setTraceBased(true);
      }
      if (model.getMinimumSeverity() != null) {
        configBuilder.setMinimumSeverity(severityNumberToSeverity(model.getMinimumSeverity()));
      }
      return configBuilder.build();
    }
  }

  // Visible for testing
  static Severity severityNumberToSeverity(SeverityNumber model) {
    switch (model) {
      case TRACE:
        return Severity.TRACE;
      case TRACE_2:
        return Severity.TRACE2;
      case TRACE_3:
        return Severity.TRACE3;
      case TRACE_4:
        return Severity.TRACE4;
      case DEBUG:
        return Severity.DEBUG;
      case DEBUG_2:
        return Severity.DEBUG2;
      case DEBUG_3:
        return Severity.DEBUG3;
      case DEBUG_4:
        return Severity.DEBUG4;
      case INFO:
        return Severity.INFO;
      case INFO_2:
        return Severity.INFO2;
      case INFO_3:
        return Severity.INFO3;
      case INFO_4:
        return Severity.INFO4;
      case WARN:
        return Severity.WARN;
      case WARN_2:
        return Severity.WARN2;
      case WARN_3:
        return Severity.WARN3;
      case WARN_4:
        return Severity.WARN4;
      case ERROR:
        return Severity.ERROR;
      case ERROR_2:
        return Severity.ERROR2;
      case ERROR_3:
        return Severity.ERROR3;
      case ERROR_4:
        return Severity.ERROR4;
      case FATAL:
        return Severity.FATAL;
      case FATAL_2:
        return Severity.FATAL2;
      case FATAL_3:
        return Severity.FATAL3;
      case FATAL_4:
        return Severity.FATAL4;
    }
    throw new IllegalArgumentException("Unrecognized severity number: " + model);
  }
}
