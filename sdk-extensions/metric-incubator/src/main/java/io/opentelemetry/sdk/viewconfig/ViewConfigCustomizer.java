/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** SPI implementation for loading view configuration YAML. */
public final class ViewConfigCustomizer implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addMeterProviderCustomizer(ViewConfigCustomizer::customizeMeterProvider);
  }

  // Visible for testing
  static SdkMeterProviderBuilder customizeMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder, ConfigProperties configProperties) {
    List<String> configFileLocations =
        configProperties.getList("otel.experimental.metrics.view.config");
    for (String configFileLocation : configFileLocations) {
      if (configFileLocation.startsWith("classpath:")) {
        String classpathLocation = configFileLocation.substring("classpath:".length());
        InputStream inputStream = ViewConfigCustomizer.class.getResourceAsStream(classpathLocation);
        if (inputStream == null) {
          throw new ConfigurationException("Resource not found on classpath: " + classpathLocation);
        }
        BufferedReader bufferedReader =
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        ViewConfig.registerViews(meterProviderBuilder, bufferedReader);
      } else {
        File file = new File(configFileLocation);
        ViewConfig.registerViews(meterProviderBuilder, file);
      }
    }
    return meterProviderBuilder;
  }
}
