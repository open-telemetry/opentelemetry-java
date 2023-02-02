/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LoggerProviderConfigurationTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureLoggerProvider() {
    Map<String, String> properties =
        ImmutableMap.of(
            "otel.logs.exporter", "none",
            "otel.attribute.count.limit", "5");
    List<Closeable> closeables = new ArrayList<>();

    // We don't have any exporters on classpath for this test so check no-op case. Exporter cases
    // are verified in other test sets like testFullConfig.
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
    LoggerProviderConfiguration.configureLoggerProvider(
        builder,
        DefaultConfigProperties.createForTest(properties),
        LoggerProviderConfiguration.class.getClassLoader(),
        MeterProvider.noop(),
        (a, unused) -> a,
        closeables);
    cleanup.addCloseables(closeables);

    try (SdkLoggerProvider loggerProvider = builder.build()) {
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
      assertThat(closeables).isEmpty();
    }
  }

  @Test
  void configureLogLimits() {
    assertThat(
            LoggerProviderConfiguration.configureLogLimits(
                DefaultConfigProperties.createForTest(Collections.emptyMap())))
        .isEqualTo(LogLimits.getDefault());

    LogLimits config =
        LoggerProviderConfiguration.configureLogLimits(
            DefaultConfigProperties.createForTest(
                ImmutableMap.of(
                    "otel.attribute.value.length.limit", "100",
                    "otel.attribute.count.limit", "5")));
    assertThat(config.getMaxAttributeValueLength()).isEqualTo(100);
    assertThat(config.getMaxNumberOfAttributes()).isEqualTo(5);
  }
}
