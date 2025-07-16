/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Objects;
import java.util.regex.Pattern;

final class OpenTelemetryConfigurationFactory
    implements Factory<OpenTelemetryConfigurationModel, ExtendedOpenTelemetrySdk> {

  private static final Pattern SUPPORTED_FILE_FORMATS = Pattern.compile("^(0.4)|(1.0(-rc.\\d*)?)$");

  private static final OpenTelemetryConfigurationFactory INSTANCE =
      new OpenTelemetryConfigurationFactory();

  private OpenTelemetryConfigurationFactory() {}

  static OpenTelemetryConfigurationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ExtendedOpenTelemetrySdk create(
      OpenTelemetryConfigurationModel model, DeclarativeConfigContext context) {
    ExtendedOpenTelemetrySdkBuilder builder = ExtendedOpenTelemetrySdk.builder();
    String fileFormat = model.getFileFormat();
    if (fileFormat == null || !SUPPORTED_FILE_FORMATS.matcher(fileFormat).matches()) {
      throw new DeclarativeConfigException(
          "Unsupported file format. Supported formats include 0.4, 1.0*");
    }
    // TODO(jack-berg): log warning if version is not exact match, which may result in unexpected
    // behavior for experimental properties.

    if (Objects.equals(true, model.getDisabled())) {
      return builder.build();
    }

    builder.setCloseableConsumer(context::addCloseable);
    builder.setConfigProvider(SdkConfigProvider.create(model, context.getComponentLoader()));

    if (model.getPropagator() != null) {
      builder.setPropagators(
          PropagatorFactory.getInstance().create(model.getPropagator(), context));
    }

    Resource resource;
    if (model.getResource() != null) {
      resource = ResourceFactory.getInstance().create(model.getResource(), context);
    } else {
      resource = Resource.getDefault();
    }

    if (model.getLoggerProvider() != null) {
      builder.withLoggerProvider(
          sdkLoggerProviderBuilder ->
              LoggerProviderFactory.getInstance()
                  .configure(
                      sdkLoggerProviderBuilder.setResource(resource),
                      LoggerProviderAndAttributeLimits.create(
                          model.getAttributeLimits(), model.getLoggerProvider()),
                      context));
    }

    if (model.getTracerProvider() != null) {
      builder.withTracerProvider(
          sdkTracerProviderBuilder ->
              TracerProviderFactory.getInstance()
                  .configure(
                      sdkTracerProviderBuilder.setResource(resource),
                      TracerProviderAndAttributeLimits.create(
                          model.getAttributeLimits(), model.getTracerProvider()),
                      context));
    }

    if (model.getMeterProvider() != null) {
      builder.withMeterProvider(
          sdkMeterProviderBuilder ->
              MeterProviderFactory.getInstance()
                  .configure(
                      sdkMeterProviderBuilder.setResource(resource),
                      model.getMeterProvider(),
                      context));
    }

    return builder.build();
  }
}
