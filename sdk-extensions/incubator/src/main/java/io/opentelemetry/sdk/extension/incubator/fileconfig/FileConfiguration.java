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
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.yaml.snakeyaml.Yaml;

/**
 * Configure {@link OpenTelemetrySdk} from YAML configuration files conforming to the schema in <a
 * href="https://github.com/open-telemetry/opentelemetry-configuration">open-telemetry/opentelemetry-configuration</a>.
 *
 * @see #parseAndCreate(InputStream)
 */
public final class FileConfiguration {

  private static final Logger logger = Logger.getLogger(FileConfiguration.class.getName());
  private static final Pattern ENV_VARIABLE_REFERENCE =
      Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)}");

  private static final ObjectMapper MAPPER;

  static {
    MAPPER =
        new ObjectMapper()
            // Create empty object instances for keys which are present but have null values
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
    // Boxed primitives which are present but have null values should be set to null, rather than
    // empty instances
    MAPPER.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Integer.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Double.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Boolean.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
  }

  private FileConfiguration() {}

  /**
   * Combines {@link #parse(InputStream)} and {@link #create(OpenTelemetryConfiguration)}.
   *
   * @throws ConfigurationException if unable to parse or interpret
   */
  public static OpenTelemetrySdk parseAndCreate(InputStream inputStream) {
    OpenTelemetryConfiguration configurationModel = parse(inputStream);
    return create(configurationModel);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @return the {@link OpenTelemetrySdk}
   * @throws ConfigurationException if unable to interpret
   */
  public static OpenTelemetrySdk create(OpenTelemetryConfiguration configurationModel) {
    List<Closeable> closeables = new ArrayList<>();
    try {
      return OpenTelemetryConfigurationFactory.getInstance()
          .create(
              configurationModel,
              SpiHelper.create(FileConfiguration.class.getClassLoader()),
              closeables);
    } catch (RuntimeException e) {
      logger.info(
          "Error encountered interpreting configuration model. Closing partially configured components.");
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
   * Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfiguration}.
   *
   * <p>Before parsing, environment variable substitution is performed as described in {@link
   * EnvSubstitutionConstructor}.
   *
   * @throws ConfigurationException if unable to parse
   */
  public static OpenTelemetryConfiguration parse(InputStream configuration) {
    try {
      return parse(configuration, System.getenv());
    } catch (RuntimeException e) {
      throw new ConfigurationException("Unable to parse configuration input stream", e);
    }
  }

  // Visible for testing
  static OpenTelemetryConfiguration parse(
      InputStream configuration, Map<String, String> environmentVariables) {
    Object yamlObj = loadYaml(configuration, environmentVariables);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfiguration.class);
  }

  // Visible for testing
  static Object loadYaml(InputStream inputStream, Map<String, String> environmentVariables) {
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings, new EnvSubstitutionConstructor(settings, environmentVariables));
    return yaml.loadFromInputStream(inputStream);
  }

  /**
   * Convert the {@code model} to a generic {@link StructuredConfigProperties}, which can be used to
   * read configuration not part of the model.
   *
   * @param model the configuration model
   * @return a generic {@link StructuredConfigProperties} representation of the model
   */
  public static StructuredConfigProperties toConfigProperties(OpenTelemetryConfiguration model) {
    return toConfigProperties((Object) model);
  }

  static StructuredConfigProperties toConfigProperties(Object model) {
    Map<String, Object> configurationMap =
        MAPPER.convertValue(model, new TypeReference<Map<String, Object>>() {});
    return YamlStructuredConfigProperties.create(configurationMap);
  }

  /**
   * {@link StandardConstructor} which substitutes environment variables.
   *
   * <p>Environment variables follow the syntax {@code ${VARIABLE}}, where {@code VARIABLE} is an
   * environment variable matching the regular expression {@code [a-zA-Z_]+[a-zA-Z0-9_]*}.
   *
   * <p>Environment variable substitution only takes place on scalar values of maps. References to
   * environment variables in keys or sets are ignored.
   *
   * <p>If a referenced environment variable is not defined, it is replaced with {@code ""}.
   */
  private static final class EnvSubstitutionConstructor extends StandardConstructor {

    // Yaml is not thread safe but this instance is always used on the same thread
    private final Yaml yaml = new Yaml();
    private final Map<String, String> environmentVariables;

    private EnvSubstitutionConstructor(
        LoadSettings loadSettings, Map<String, String> environmentVariables) {
      super(loadSettings);
      this.environmentVariables = environmentVariables;
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
      // First call the super to construct mapping from MappingNode as usual
      Map<Object, Object> result = super.constructMapping(node);

      // Iterate through the map entries, and:
      // 1. Identify entries which are scalar strings eligible for environment variable substitution
      // 2. Apply environment variable substitution
      // 3. Re-parse substituted value so it has correct type (i.e. yaml.load(newVal))
      for (Map.Entry<Object, Object> entry : result.entrySet()) {
        Object value = entry.getValue();
        if (!(value instanceof String)) {
          continue;
        }

        String val = (String) value;
        Matcher matcher = ENV_VARIABLE_REFERENCE.matcher(val);
        if (!matcher.find()) {
          continue;
        }

        int offset = 0;
        StringBuilder newVal = new StringBuilder();
        do {
          MatchResult matchResult = matcher.toMatchResult();
          String replacement = environmentVariables.getOrDefault(matcher.group(1), "");
          newVal.append(val, offset, matchResult.start()).append(replacement);
          offset = matchResult.end();
        } while (matcher.find());
        if (offset != val.length()) {
          newVal.append(val, offset, val.length());
        }
        entry.setValue(yaml.load(newVal.toString()));
      }

      return result;
    }
  }
}
