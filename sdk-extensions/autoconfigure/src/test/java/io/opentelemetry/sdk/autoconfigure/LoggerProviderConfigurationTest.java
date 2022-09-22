/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class LoggerProviderConfigurationTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());

  @Test
  void configureLoggerProvider() {
    Map<String, String> properties =
        ImmutableMap.of(
            "otel.logs.exporter", "none",
            "otel.attribute.count.limit", "5");

    // We don't have any exporters on classpath for this test so check no-op case. Exporter cases
    // are verified in other test sets like testFullConfig.
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
    LoggerProviderConfiguration.configureLoggerProvider(
        builder,
        DefaultConfigProperties.createForTest(properties),
        LoggerProviderConfiguration.class.getClassLoader(),
        MeterProvider.noop(),
        (a, unused) -> a);
    SdkLoggerProvider loggerProvider = builder.build();

    try {
      assertThat(loggerProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState -> {
                assertThat(sharedState)
                    .extracting("logRecordProcessor")
                    .isEqualTo(LogRecordProcessor.composite());
                assertThat(sharedState)
                    .extracting(
                        "logLimitsSupplier", as(InstanceOfAssertFactories.type(Supplier.class)))
                    .extracting(supplier -> (LogLimits) supplier.get())
                    .isEqualTo(LogLimits.builder().setMaxNumberOfAttributes(5).build());
              });
    } finally {
      loggerProvider.shutdown();
    }
  }

  @Test
  void configureSpanLimits() {
    assertThat(LoggerProviderConfiguration.configureLogLimits(EMPTY))
        .isEqualTo(LogLimits.getDefault());

    SpanLimits config =
        TracerProviderConfiguration.configureSpanLimits(
            DefaultConfigProperties.createForTest(
                ImmutableMap.of(
                    "otel.attribute.value.length.limit", "100",
                    "otel.attribute.count.limit", "5")));
    assertThat(config.getMaxAttributeValueLength()).isEqualTo(100);
    assertThat(config.getMaxNumberOfAttributes()).isEqualTo(5);
  }
}
