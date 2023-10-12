/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Parses YAML configuration files conforming to the schema in <a
 * href="https://github.com/open-telemetry/opentelemetry-configuration">open-telemetry/opentelemetry-configuration</a>
 * to a {@link OpenTelemetryConfiguration} in-memory representation. Interprets the in-memory
 * representation to produce an {@link OpenTelemetrySdk}.
 *
 * @see #parseAndInterpret(InputStream)
 */
public final class ConfigurationFactory {

  static final ObjectMapper MAPPER =
      new ObjectMapper()
          // Create empty object instances for keys which are present but have null values
          .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

  private static final Logger logger = Logger.getLogger(ConfigurationFactory.class.getName());

  private ConfigurationFactory() {}

  /**
   * Combines {@link #parse(InputStream)} and {@link #interpret(OpenTelemetryConfiguration)}
   * operations.
   */
  public static OpenTelemetrySdk parseAndInterpret(InputStream inputStream) {
    OpenTelemetryConfiguration model;
    try {
      model = parse(inputStream);
    } catch (RuntimeException e) {
      throw new ConfigurationException("Unable to parse inputStream", e);
    }

    return interpret(model);
  }

  /** Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfiguration}. */
  public static OpenTelemetryConfiguration parse(InputStream configuration) {
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings);
    Object yamlObj = yaml.loadFromInputStream(configuration);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfiguration.class);
  }

  /**
   * Interpret the {@code model} to create {@link OpenTelemetrySdk} instance corresonding to
   * configuration.
   *
   * @param model the configuration model
   * @return the {@link OpenTelemetrySdk}
   */
  public static OpenTelemetrySdk interpret(OpenTelemetryConfiguration model) {
    List<Closeable> closeables = new ArrayList<>();
    try {
      return OpenTelemetryConfigurationFactory.getInstance()
          .create(model, SpiHelper.create(ConfigurationFactory.class.getClassLoader()), closeables);
    } catch (RuntimeException e) {
      logger.info(
          "Error encountered interpreting configuration. Closing partially configured components.");
      for (Closeable closeable : closeables) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.warning(
              "Error closing " + closeable.getClass().getName() + ": " + ex.getMessage());
        }
      }
      if (e instanceof ConfigurationException) {
        throw e;
      }
      throw new ConfigurationException("Unexpected configuration error", e);
    }
  }

  /**
   * Convert the {@code model} to a generic {@link ExtendedConfigProperties}, which can be used to
   * read configuration not part of the model.
   *
   * @param model the configuration model
   * @return a generic {@link ExtendedConfigProperties} representation of the model
   */
  public static ExtendedConfigProperties toConfigProperties(OpenTelemetryConfiguration model) {
    Map<String, Object> configurationMap =
        ConfigurationFactory.MAPPER.convertValue(
            model, new TypeReference<Map<String, Object>>() {});
    return new FileConfigProperties(configurationMap);
  }
}
