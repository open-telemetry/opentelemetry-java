/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

class TestExtendedOpenTelemetrySdk {
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();

  @Test
  void extractExtendedSdk() {
    SdkConfigProvider configProvider =
        SdkConfigProvider.create(new OpenTelemetryConfigurationModel());
    OpenTelemetrySdk sdk =
        ExtendedOpenTelemetrySdk.builder()
            .withMeterProvider(builder -> builder.registerMetricReader(sdkMeterReader))
            .setConfigProvider(configProvider)
            .build();
    ExtendedOpenTelemetrySdk extendedSdk = ExtendedOpenTelemetrySdk.fromOpenTelemetrySdk(sdk);
    assertThat(extendedSdk).isNotNull();
    assertThat(extendedSdk.getConfigProvider()).isEqualTo(configProvider);
  }
}
