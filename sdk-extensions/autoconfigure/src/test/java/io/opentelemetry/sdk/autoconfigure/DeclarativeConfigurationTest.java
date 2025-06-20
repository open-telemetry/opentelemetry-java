/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeclarativeConfigurationTest {

  @Test
  void configFile(@TempDir Path tempDir) throws IOException {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          console: {}\n";
    Path path = tempDir.resolve("otel-config.yaml");
    Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", path.toString()));

    assertThatThrownBy(() -> AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "Cannot autoconfigure from config file without opentelemetry-sdk-extension-incubator on the classpath");
  }
}
