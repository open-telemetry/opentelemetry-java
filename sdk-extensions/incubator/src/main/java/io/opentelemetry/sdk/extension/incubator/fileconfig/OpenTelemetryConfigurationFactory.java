/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.Objects;

final class OpenTelemetryConfigurationFactory
    implements Factory<OpenTelemetryConfigurationModel, OpenTelemetrySdk> {

  private static final OpenTelemetryConfigurationFactory INSTANCE =
      new OpenTelemetryConfigurationFactory();

  private OpenTelemetryConfigurationFactory() {}

  static OpenTelemetryConfigurationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public OpenTelemetrySdk create(
      OpenTelemetryConfigurationModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();
    if (!"0.3".equals(model.getFileFormat())) {
      throw new ConfigurationException("Unsupported file format. Supported formats include: 0.3");
    }

    if (Objects.equals(Boolean.TRUE, model.getDisabled())) {
      return builder.build();
    }

    if (model.getPropagator() != null) {
      builder.setPropagators(
          PropagatorFactory.getInstance().create(model.getPropagator(), spiHelper, closeables));
    }

    Resource resource = Resource.getDefault();
    if (model.getResource() != null) {
      resource = ResourceFactory.getInstance().create(model.getResource(), spiHelper, closeables);
    }

    if (model.getLoggerProvider() != null) {
      builder.setLoggerProvider(
          FileConfigUtil.addAndReturn(
              closeables,
              LoggerProviderFactory.getInstance()
                  .create(
                      LoggerProviderAndAttributeLimits.create(
                          model.getAttributeLimits(), model.getLoggerProvider()),
                      spiHelper,
                      closeables)
                  .setResource(resource)
                  .build()));
    }

    if (model.getTracerProvider() != null) {
      builder.setTracerProvider(
          FileConfigUtil.addAndReturn(
              closeables,
              TracerProviderFactory.getInstance()
                  .create(
                      TracerProviderAndAttributeLimits.create(
                          model.getAttributeLimits(), model.getTracerProvider()),
                      spiHelper,
                      closeables)
                  .setResource(resource)
                  .build()));
    }

    if (model.getMeterProvider() != null) {
      builder.setMeterProvider(
          FileConfigUtil.addAndReturn(
              closeables,
              MeterProviderFactory.getInstance()
                  .create(model.getMeterProvider(), spiHelper, closeables)
                  .setResource(resource)
                  .build()));
    }

    return FileConfigUtil.addAndReturn(closeables, builder.build());
  }
}
