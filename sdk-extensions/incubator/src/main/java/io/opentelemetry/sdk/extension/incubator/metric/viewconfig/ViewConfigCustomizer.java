/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
        try (InputStream inputStream =
            ViewConfigCustomizer.class.getResourceAsStream(classpathLocation)) {
          if (inputStream == null) {
            throw new ConfigurationException(
                "Resource "
                    + classpathLocation
                    + " not found on classpath of classloader "
                    + ViewConfigCustomizer.class.getClassLoader().getClass().getName());
          }
          ViewConfig.registerViews(meterProviderBuilder, inputStream);
        } catch (IOException e) {
          throw new ConfigurationException(
              "An error occurred reading view config resource on classpath: " + classpathLocation,
              e);
        }
      } else {
        try (FileInputStream fileInputStream = new FileInputStream(configFileLocation)) {
          ViewConfig.registerViews(meterProviderBuilder, fileInputStream);
        } catch (FileNotFoundException e) {
          throw new ConfigurationException("View config file not found: " + configFileLocation, e);
        } catch (IOException e) {
          throw new ConfigurationException(
              "An error occurred reading view config file: " + configFileLocation, e);
        }
      }
    }
    return meterProviderBuilder;
  }
}
