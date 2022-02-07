/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.SdkLogEmitterProviderBuilder;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LogEmitterProviderConfigurationTest {

  @Test
  void configureLogEmitterProvider() {
    Map<String, String> properties = Collections.singletonMap("otel.logs.exporter", "none");

    // We don't have any exporters on classpath for this test so check no-op case. Exporter cases
    // are verified in other test sets like testFullConfig.
    SdkLogEmitterProviderBuilder builder = SdkLogEmitterProvider.builder();
    LogEmitterProviderConfiguration.configureLogEmitterProvider(
        builder,
        DefaultConfigProperties.createForTest(properties),
        MeterProvider.noop(),
        (a, unused) -> a);
    SdkLogEmitterProvider logEmitterProvider = builder.build();

    try {
      assertThat(logEmitterProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState ->
                  assertThat(sharedState)
                      .extracting("logProcessor")
                      .isEqualTo(LogProcessor.composite()));
    } finally {
      logEmitterProvider.shutdown();
    }
  }
}
