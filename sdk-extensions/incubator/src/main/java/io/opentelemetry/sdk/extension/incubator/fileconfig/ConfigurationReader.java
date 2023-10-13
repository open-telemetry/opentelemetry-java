/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

final class ConfigurationReader {

  private static final Pattern ENV_VARIABLE_REFERENCE =
      Pattern.compile("\\$\\{env:([a-zA-Z_]+[a-zA-Z0-9_]*)}");

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

  private ConfigurationReader() {}

  /**
   * Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfiguration}.
   *
   * <p>Before parsing, environment variable substitution is performed as described in {@link
   * #substituteEnvVariables(InputStream, Map)}.
   */
  static OpenTelemetryConfiguration parse(InputStream configuration) {
    return parse(configuration, System.getenv());
  }

  // Visible for testing
  static OpenTelemetryConfiguration parse(
      InputStream configuration, Map<String, String> environmentVariables) {
    Object yamlObj = loadYaml(configuration, environmentVariables);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfiguration.class);
  }

  static Object loadYaml(InputStream inputStream, Map<String, String> environmentVariables) {
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings);
    String withEnvironmentVariablesSubstituted =
        substituteEnvVariables(inputStream, environmentVariables);
    return yaml.loadFromString(withEnvironmentVariablesSubstituted);
  }

  /**
   * Read the input and substitute any environment variables.
   *
   * <p>Environment variables follow the syntax {@code ${env:VARIABLE}}, where {@code VARIABLE} is
   * an environment variable matching the regular expression {@code [a-zA-Z_]+[a-zA-Z0-9_]*}.
   *
   * <p>If a referenced environment variable is not defined, it is replaced with {@code ""}.
   *
   * @return the string contents of the {@code inputStream} with environment variables substituted
   */
  static String substituteEnvVariables(
      InputStream inputStream, Map<String, String> environmentVariables) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        Matcher matcher = ENV_VARIABLE_REFERENCE.matcher(line);
        if (matcher.find()) {
          int offset = 0;
          StringBuilder newLine = new StringBuilder();
          do {
            MatchResult matchResult = matcher.toMatchResult();
            String replacement = environmentVariables.getOrDefault(matcher.group(1), "");
            newLine.append(line, offset, matchResult.start()).append(replacement);
            offset = matchResult.end();
          } while (matcher.find());
          if (offset != line.length()) {
            newLine.append(line, offset, line.length());
          }
          line = newLine.toString();
        }
        stringBuilder.append(line).append(System.lineSeparator());
      }
      return stringBuilder.toString();
    } catch (IOException e) {
      throw new ConfigurationException("Error reading input stream", e);
    }
  }
}
