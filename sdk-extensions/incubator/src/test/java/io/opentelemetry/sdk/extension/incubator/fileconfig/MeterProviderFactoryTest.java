/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MeterProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(MeterProviderFactoryTest.class.getClassLoader());

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    SdkMeterProvider expectedProvider = SdkMeterProvider.builder().build();
    cleanup.addCloseable(expectedProvider);

    SdkMeterProvider provider =
        MeterProviderFactory.getInstance()
            .create(new MeterProvider(), spiHelper, closeables)
            .build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  @Test
  void create_Configured() {
    List<Closeable> closeables = new ArrayList<>();
    SdkMeterProvider expectedProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(
                io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                        OtlpGrpcMetricExporter.getDefault())
                    .build())
            .registerView(
                InstrumentSelector.builder().setName("instrument-name").build(),
                View.builder().setName("stream-name").build())
            .build();
    cleanup.addCloseable(expectedProvider);

    SdkMeterProvider provider =
        MeterProviderFactory.getInstance()
            .create(
                new MeterProvider()
                    .withReaders(
                        Collections.singletonList(
                            new MetricReader()
                                .withPeriodic(
                                    new PeriodicMetricReader()
                                        .withExporter(
                                            new MetricExporter().withOtlp(new OtlpMetric())))))
                    .withViews(
                        Collections.singletonList(
                            new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                    .View()
                                .withSelector(new Selector().withInstrumentName("instrument-name"))
                                .withStream(
                                    new Stream().withName("stream-name").withAttributeKeys(null)))),
                spiHelper,
                closeables)
            .build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }
}
