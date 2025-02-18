package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;

public interface OpenTelemetryConfigurationModelCustomizerProvider extends Ordered {
  OpenTelemetryConfigurationModel customize(OpenTelemetryConfigurationModel model);
}
