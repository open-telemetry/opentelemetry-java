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
    if (model == null) {
      return FileConfigUtil.addAndReturn(closeables, OpenTelemetrySdk.builder().build());
    }

    if (!"0.1".equals(model.getFileFormat())) {
      throw new ConfigurationException("Unsupported file format. Supported formats include: 0.1");
    }

    OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();

    builder.setPropagators(
        PropagatorsFactory.getInstance().create(model.getPropagators(), spiHelper, closeables));

    Resource resource =
        ResourceFactory.getInstance().create(model.getResource(), spiHelper, closeables);

    if (model.getLoggerProvider() != null) {
      builder.setLoggerProvider(
          FileConfigUtil.addAndReturn(
              closeables,
              LoggerProviderFactory.getInstance()
                  .create(model.getLoggerProvider(), spiHelper, closeables)
                  .setResource(resource)
                  .build()));
    }

    if (model.getTracerProvider() != null) {
      builder.setTracerProvider(
          FileConfigUtil.addAndReturn(
              closeables,
              TracerProviderFactory.getInstance()
                  .create(model.getTracerProvider(), spiHelper, closeables)
                  .setResource(resource)
                  .build()));
    }

    // TODO(jack-berg): add support for meter provider
    // TODO(jack-berg): add support for propagators
    // TODO(jack-berg): add support for resource
    // TODO(jack-berg): add support for general attribute limits

    return FileConfigUtil.addAndReturn(closeables, builder.build());
  }
}
