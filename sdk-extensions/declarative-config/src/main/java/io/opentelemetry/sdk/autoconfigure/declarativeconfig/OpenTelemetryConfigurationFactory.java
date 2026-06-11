/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel;
import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.internal.OpenTelemetrySdkBuilderUtil;
import io.opentelemetry.sdk.internal.SdkConfigProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

final class OpenTelemetryConfigurationFactory
    implements Factory<OpenTelemetryConfigurationModel, DeclarativeConfigResult> {

  private static final Logger logger =
      Logger.getLogger(OpenTelemetryConfigurationFactory.class.getName());
  private static final Pattern SUPPORTED_FILE_FORMATS = Pattern.compile("^(0.4)|(1.0(-rc.\\d*)?)$");
  private static final String EXPECTED_FILE_FORMAT = "1.0";

  private static final OpenTelemetryConfigurationFactory INSTANCE =
      new OpenTelemetryConfigurationFactory();

  private OpenTelemetryConfigurationFactory() {}

  static OpenTelemetryConfigurationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public DeclarativeConfigResult create(
      OpenTelemetryConfigurationModel model, DeclarativeConfigContext context) {
    DeclarativeConfigProperties modelProperties =
        DeclarativeConfiguration.toConfigProperties(model, context.getDelegateComponentLoader());
    SdkConfigProvider sdkConfigProvider = SdkConfigProvider.create(modelProperties);
    context.setConfigProvider(sdkConfigProvider);
    OpenTelemetrySdkBuilder builder =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
            OpenTelemetrySdk.builder(), sdkConfigProvider);
    String fileFormat = model.getFileFormat();
    if (fileFormat == null || !SUPPORTED_FILE_FORMATS.matcher(fileFormat).matches()) {
      throw new DeclarativeConfigException(
          "Unsupported file format '" + fileFormat + "'. Supported formats include 0.4, 1.0*");
    }
    if (!EXPECTED_FILE_FORMAT.equals(fileFormat)) {
      logger.warning(
          "Configuration file_format '"
              + fileFormat
              + "' does not exactly match expected version '"
              + EXPECTED_FILE_FORMAT
              + "'. This may result in unexpected behavior for experimental properties.");
    }

    if (Objects.equals(true, model.getDisabled())) {
      return new DeclarativeConfigResult(
          (ExtendedOpenTelemetrySdk) builder.build(), Resource.getDefault());
    }

    if (model.getPropagator() != null) {
      builder.setPropagators(
          PropagatorFactory.getInstance().create(model.getPropagator(), context));
    }

    Resource resource = Resource.getDefault();
    if (model.getResource() != null) {
      resource = ResourceFactory.getInstance().create(model.getResource(), context);
    }

    MeterProviderModel meterProviderModel = model.getMeterProvider();
    if (meterProviderModel == null) {
      meterProviderModel = new MeterProviderModel();
    }
    SdkMeterProvider meterProvider =
        MeterProviderFactory.getInstance()
            .create(meterProviderModel, context)
            .setResource(resource)
            .build();
    context.setMeterProvider(meterProvider);
    builder.setMeterProvider(context.addCloseable(meterProvider));

    LoggerProviderModel loggerProviderModel = model.getLoggerProvider();
    if (loggerProviderModel == null) {
      loggerProviderModel = new LoggerProviderModel();
    }
    builder.setLoggerProvider(
        context.addCloseable(
            LoggerProviderFactory.getInstance()
                .create(
                    LoggerProviderAndAttributeLimits.create(
                        model.getAttributeLimits(), loggerProviderModel),
                    context)
                .setResource(resource)
                .build()));

    TracerProviderModel tracerProviderModel = model.getTracerProvider();
    if (tracerProviderModel == null) {
      tracerProviderModel = new TracerProviderModel();
    }
    builder.setTracerProvider(
        context.addCloseable(
            TracerProviderFactory.getInstance()
                .create(
                    TracerProviderAndAttributeLimits.create(
                        model.getAttributeLimits(), tracerProviderModel),
                    context)
                .setResource(resource)
                .build()));

    return new DeclarativeConfigResult(
        (ExtendedOpenTelemetrySdk) context.addCloseable(builder.build()), resource);
  }
}
