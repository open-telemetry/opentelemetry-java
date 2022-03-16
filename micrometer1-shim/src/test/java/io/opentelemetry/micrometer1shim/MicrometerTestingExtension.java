/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import java.util.Collection;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class MicrometerTestingExtension implements AfterEachCallback, BeforeEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(MicrometerTestingExtension.class);

  private final InMemoryMetricReader metricReader = InMemoryMetricReader.create();

  Collection<MetricData> collectAllMetrics() {
    return metricReader.collectAllMetrics();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    ExtensionContext.Store store = context.getStore(NAMESPACE);

    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(metricReader)
            .setMinimumCollectionInterval(Duration.ZERO)
            .build();
    MeterRegistry otelMeterRegistry =
        configureOtelRegistry(
                OpenTelemetryMeterRegistry.builder(
                    OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build()))
            .build();
    configureMeterRegistry(otelMeterRegistry);

    store.put(SdkMeterProvider.class, meterProvider);
    store.put(MeterRegistry.class, otelMeterRegistry);

    Metrics.addRegistry(otelMeterRegistry);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    ExtensionContext.Store store = context.getStore(NAMESPACE);
    MeterRegistry otelMeterRegistry = store.get(MeterRegistry.class, MeterRegistry.class);
    SdkMeterProvider meterProvider = store.get(SdkMeterProvider.class, SdkMeterProvider.class);

    Metrics.removeRegistry(otelMeterRegistry);
    meterProvider.close();

    Metrics.globalRegistry.forEachMeter(Metrics.globalRegistry::remove);
  }

  OpenTelemetryMeterRegistryBuilder configureOtelRegistry(
      OpenTelemetryMeterRegistryBuilder registry) {
    return registry;
  }

  MeterRegistry configureMeterRegistry(MeterRegistry registry) {
    return registry;
  }
}
