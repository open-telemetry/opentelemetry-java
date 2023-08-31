/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

final class OpenTelemetryConfigurationFactory
    implements Factory<OpenTelemetryConfiguration, OpenTelemetrySdk> {

  private static final OpenTelemetryConfigurationFactory INSTANCE =
      new OpenTelemetryConfigurationFactory();

  private OpenTelemetryConfigurationFactory() {}

  static OpenTelemetryConfigurationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public OpenTelemetrySdk create(
      @Nullable OpenTelemetryConfiguration model, SpiHelper spiHelper, List<Closeable> closeables) {
    OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();
    if (model == null) {
      return FileConfigUtil.addAndReturn(closeables, builder.build());
    }

    if (!"0.1".equals(model.getFileFormat())) {
      throw new ConfigurationException("Unsupported file format. Supported formats include: 0.1");
    }

    if (Objects.equals(Boolean.TRUE, model.getDisabled())) {
      return builder.build();
    }

    builder.setPropagators(
        PropagatorsFactory.getInstance().create(model.getPropagators(), spiHelper, closeables));

    Resource resource =
        ResourceFactory.getInstance().create(model.getResource(), spiHelper, closeables);

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
