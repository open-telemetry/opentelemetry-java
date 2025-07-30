/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.TraceBasedLogRecordProcessorComponentProvider;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.TraceBasedLogRecordProcessor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TraceBasedLogRecordProcessorComponentProviderTest {

  @Test
  void createTraceBasedProcessor_DirectComponentProvider() {
    TraceBasedLogRecordProcessorComponentProvider provider =
        new TraceBasedLogRecordProcessorComponentProvider();

    assertThat(provider.getType()).isEqualTo(LogRecordProcessor.class);
    assertThat(provider.getName()).isEqualTo("trace_based");
  }

  @Test
  void createTraceBasedProcessor_ValidConfig() {
    DeclarativeConfigProperties config =
        getConfig(
            "processors:\n" // this comment exists only to influence spotless formatting
                + "  - simple:\n"
                + "      exporter:\n"
                + "        console: {}\n");

    TraceBasedLogRecordProcessorComponentProvider provider =
        new TraceBasedLogRecordProcessorComponentProvider();

    LogRecordProcessor processor = provider.create(config);

    assertThat(processor).isInstanceOf(TraceBasedLogRecordProcessor.class);

    assertThat(processor.toString())
        .contains("TraceBasedLogRecordProcessor")
        .contains("delegate=SimpleLogRecordProcessor")
        .contains("logRecordExporter=SystemOutLogRecordExporter");
  }

  @Test
  void createTraceBasedProcessor_MissingProcessors() {
    DeclarativeConfigProperties config = getConfig("");

    TraceBasedLogRecordProcessorComponentProvider provider =
        new TraceBasedLogRecordProcessorComponentProvider();

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("At least one processor is required for trace_based log processors");
  }

  @Test
  void createTraceBasedProcessor_EmptyProcessors() {
    DeclarativeConfigProperties config = getConfig("processors: []\n");

    TraceBasedLogRecordProcessorComponentProvider provider =
        new TraceBasedLogRecordProcessorComponentProvider();

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("At least one processor is required for trace_based log processors");
  }

  @Test
  void createTraceBasedProcessor_MultipleProcessors() {
    DeclarativeConfigProperties config =
        getConfig(
            "processors:\n"
                + "  - simple:\n"
                + "      exporter:\n"
                + "        console: {}\n"
                + "  - simple:\n"
                + "      exporter:\n"
                + "        console: {}\n");

    TraceBasedLogRecordProcessorComponentProvider provider =
        new TraceBasedLogRecordProcessorComponentProvider();

    LogRecordProcessor processor = provider.create(config);

    assertThat(processor).isInstanceOf(TraceBasedLogRecordProcessor.class);
    assertThat(processor.toString()).contains("TraceBasedLogRecordProcessor");
  }

  private static DeclarativeConfigProperties getConfig(String yaml) {
    Object yamlObj =
        DeclarativeConfiguration.loadYaml(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap());

    return DeclarativeConfiguration.toConfigProperties(
        yamlObj,
        ComponentLoader.forClassLoader(
            TraceBasedLogRecordProcessorComponentProviderTest.class.getClassLoader()));
  }
}
