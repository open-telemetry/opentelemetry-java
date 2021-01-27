/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ConfigurableSpanExporterTest {

  @Test
  void configuration_fail() {
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "testExporter",
            ConfigProperties.createForTest(ImmutableMap.of("should.always.fail", "true")));

    assertThat(spanExporter)
        .isInstanceOf(TestConfigurableSpanExporterProvider.TestSpanExporter.class);

    assertThat(spanExporter.shutdown()).isEqualTo(CompletableResultCode.ofFailure());
  }

  @Test
  void configuration_notFail() {
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "testExporter", ConfigProperties.createForTest(Collections.emptyMap()));

    assertThat(spanExporter)
        .isInstanceOf(TestConfigurableSpanExporterProvider.TestSpanExporter.class);

    assertThat(spanExporter.shutdown()).isEqualTo(CompletableResultCode.ofSuccess());
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
