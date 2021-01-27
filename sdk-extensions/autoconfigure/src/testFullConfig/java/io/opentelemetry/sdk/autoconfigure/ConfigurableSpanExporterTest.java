/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableSpanExporterTest {

  @Test
  void configuration() {
    ConfigProperties config =
        ConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    SpanExporter spanExporter = SpanExporterConfiguration.configureExporter("testExporter", config);

    assertThat(spanExporter)
        .isInstanceOf(TestConfigurableSpanExporterProvider.TestSpanExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void exporterNotFound() {
    assertThatThrownBy(
            () ->
                SpanExporterConfiguration.configureExporter(
                    "catExporter", ConfigProperties.createForTest(Collections.emptyMap())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }
}
