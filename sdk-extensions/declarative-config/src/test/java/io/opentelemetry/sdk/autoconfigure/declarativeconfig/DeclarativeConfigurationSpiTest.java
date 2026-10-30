/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.DeclarativeConfigProviderLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.SnakeYamlDeclarativeConfigurationParser;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationMapper;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationYamlParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeclarativeConfigurationSpiTest {

  @Test
  void defaultProvidersAreRegisteredWithServiceLoader() {
    assertThat(
            DeclarativeConfigProviderLoader.load(
                DeclarativeConfigurationYamlParser.class,
                SnakeYamlDeclarativeConfigurationParser::new))
        .isInstanceOf(SnakeYamlDeclarativeConfigurationParser.class);
    DeclarativeConfigurationMapper mapper = loadMapper();
    assertThat(mapper.getClass().getName())
        .isEqualTo(
            "io.opentelemetry.sdk.autoconfigure.declarativeconfig.jackson.internal."
                + "JacksonDeclarativeConfigurationMapper");
    assertThat(mapper.order()).isEqualTo(Integer.MIN_VALUE);
  }

  @Test
  void yamlParserPerformsEnvironmentAndSystemPropertySubstitution() {
    String yaml = "boolean: ${BOOL}\nquoted: '${BOOL}'\nproperty: ${sys:port}\n";
    Map<String, String> environmentVariables = Collections.singletonMap("BOOL", "true");
    Map<Object, Object> systemProperties = new HashMap<>();
    systemProperties.put("port", "4317");

    Object parsed =
        new SnakeYamlDeclarativeConfigurationParser()
            .parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                environmentVariables,
                systemProperties);

    assertThat(parsed).isEqualTo(mapOf("boolean", true, "quoted", "true", "property", 4317));
  }

  @Test
  void mapperConvertsModelsAndGenericMaps() {
    Map<String, Object> input = new HashMap<>();
    input.put("file_format", "1.2");
    input.put("disabled", true);

    DeclarativeConfigurationMapper mapper = loadMapper();
    OpenTelemetryConfigurationModel model =
        mapper.convert(input, OpenTelemetryConfigurationModel.class);

    assertThat(model.getFileFormat()).isEqualTo("1.2");
    assertThat(model.getDisabled()).isTrue();
    assertThat(mapper.toMap(model))
        .containsEntry("file_format", "1.2")
        .containsEntry("disabled", true);
  }

  @Test
  void topLevelParseAndPropertiesUseRegisteredProviders() {
    String yaml = "file_format: '1.2'\ndisabled: true\n";
    ByteArrayInputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

    OpenTelemetryConfigurationModel model = DeclarativeConfiguration.parse(input);
    DeclarativeConfigProperties properties =
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(model.getFileFormat()).isEqualTo("1.2");
    assertThat(model.getDisabled()).isTrue();
    assertThat(properties.getString("file_format")).isEqualTo("1.2");
    assertThat(properties.getBoolean("disabled")).isTrue();
  }

  private static Map<String, Object> mapOf(Object... entries) {
    Map<String, Object> result = new HashMap<>();
    for (int i = 0; i < entries.length; i += 2) {
      result.put((String) entries[i], entries[i + 1]);
    }
    return result;
  }

  private static DeclarativeConfigurationMapper loadMapper() {
    return DeclarativeConfigProviderLoader.load(
        DeclarativeConfigurationMapper.class,
        () -> {
          throw new AssertionError("Expected a mapper provider to be registered");
        });
  }
}
