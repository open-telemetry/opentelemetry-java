/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.File;
import java.util.List;

/** SPI implementation for loading view configuration YAML. */
public class ViewConfigCustomizer implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addMeterProviderCustomizer(ViewConfigCustomizer::customizeMeterProvider);
  }

  // Visible for testing
  static SdkMeterProviderBuilder customizeMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder, ConfigProperties configProperties) {
    List<String> configFileLocations =
        configProperties.getList("otel.experimental.metric.view.config");
    for (String configFileLocation : configFileLocations) {
      File file = new File(configFileLocation);
      ViewConfig.registerViews(meterProviderBuilder, file);
    }
    return meterProviderBuilder;
  }
}
