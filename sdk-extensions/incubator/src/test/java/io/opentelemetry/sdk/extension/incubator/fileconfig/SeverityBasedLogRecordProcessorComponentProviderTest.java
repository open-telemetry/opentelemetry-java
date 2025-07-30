/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.SeverityBasedLogRecordProcessorComponentProvider;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SeverityBasedLogRecordProcessor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SeverityBasedLogRecordProcessorComponentProviderTest {

  @Test
  void createSeverityBasedProcessor_DirectComponentProvider() {
    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    assertThat(provider.getType()).isEqualTo(LogRecordProcessor.class);
    assertThat(provider.getName()).isEqualTo("severity_based");
  }

  @Test
  void createSeverityBasedProcessor_ValidConfig() {
    DeclarativeConfigProperties config =
        getConfig(
            "minimum_severity: \"WARN\"\n"
                + "delegate:\n"
                + "  simple:\n"
                + "    exporter:\n"
                + "      console: {}\n");

    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    LogRecordProcessor processor = provider.create(config);

    assertThat(processor).isInstanceOf(SeverityBasedLogRecordProcessor.class);

    assertThat(processor.toString())
        .contains("minimumSeverity=WARN")
        .contains("delegate=SimpleLogRecordProcessor")
        .contains("logRecordExporter=SystemOutLogRecordExporter");
  }

  @Test
  void createSeverityBasedProcessor_MissingMinimumSeverity() {
    DeclarativeConfigProperties config =
        getConfig(
            "delegate:\n" // this comment exists only to influence spotless formatting
                + "  simple:\n"
                + "    exporter:\n"
                + "      console: {}\n");

    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("minimum_severity is required for severity_based log processors");
  }

  @Test
  void createSeverityBasedProcessor_InvalidSeverity() {

    DeclarativeConfigProperties config =
        getConfig(
            "minimum_severity: \"INVALID\"\n"
                + "delegate:\n"
                + "  simple:\n"
                + "    exporter:\n"
                + "      console: {}\n");

    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid severity value: INVALID");
  }

  @Test
  void createSeverityBasedProcessor_MissingDelegate() {
    DeclarativeConfigProperties config = getConfig("minimum_severity: \"WARN\"\n");

    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("delegate is required for severity_based log processors");
  }

  @Test
  void createSeverityBasedProcessor_SingleDelegate() {
    DeclarativeConfigProperties config =
        getConfig(
            "minimum_severity: \"INFO\"\n"
                + "delegate:\n"
                + "  simple:\n"
                + "    exporter:\n"
                + "      console: {}\n");

    SeverityBasedLogRecordProcessorComponentProvider provider =
        new SeverityBasedLogRecordProcessorComponentProvider();

    LogRecordProcessor processor = provider.create(config);

    assertThat(processor).isInstanceOf(SeverityBasedLogRecordProcessor.class);
    assertThat(processor.toString()).contains("SeverityBasedLogRecordProcessor");
  }

  private static DeclarativeConfigProperties getConfig(String yaml) {
    Object yamlObj =
        DeclarativeConfiguration.loadYaml(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap());

    return DeclarativeConfiguration.toConfigProperties(
        yamlObj,
        ComponentLoader.forClassLoader(
            SeverityBasedLogRecordProcessorComponentProviderTest.class.getClassLoader()));
  }
}
