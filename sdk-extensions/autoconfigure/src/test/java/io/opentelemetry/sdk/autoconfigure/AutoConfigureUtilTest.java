/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Collections.singletonMap;

import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoConfigureUtilTest {

  AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk;

  @BeforeEach
  void beforeEach() {
    autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
            .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
            .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "none"))
            .build();
  }

  @Test
  void getResource() {
    Assertions.assertThat(AutoConfigureUtil.getResource(autoConfiguredOpenTelemetrySdk))
        .isSameAs(autoConfiguredOpenTelemetrySdk.getResource());
  }

  @Test
  void getConfig() {
    Assertions.assertThat(AutoConfigureUtil.getConfig(autoConfiguredOpenTelemetrySdk))
        .isSameAs(autoConfiguredOpenTelemetrySdk.getConfig());
  }
}
