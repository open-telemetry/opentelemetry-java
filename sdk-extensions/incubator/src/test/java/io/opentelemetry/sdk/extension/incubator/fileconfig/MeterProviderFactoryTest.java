/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
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
            .create(new MeterProviderModel(), spiHelper, closeables)
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
                        OtlpHttpMetricExporter.getDefault())
                    .build())
            .registerView(
                InstrumentSelector.builder().setName("instrument-name").build(),
                View.builder().setName("stream-name").build())
            .build();
    cleanup.addCloseable(expectedProvider);

    SdkMeterProvider provider =
        MeterProviderFactory.getInstance()
            .create(
                new MeterProviderModel()
                    .withReaders(
                        Collections.singletonList(
                            new MetricReaderModel()
                                .withPeriodic(
                                    new PeriodicMetricReaderModel()
                                        .withExporter(
                                            new PushMetricExporterModel()
                                                .withOtlpHttp(new OtlpHttpMetricExporterModel())))))
                    .withViews(
                        Collections.singletonList(
                            new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                    .ViewModel()
                                .withSelector(
                                    new ViewSelectorModel().withInstrumentName("instrument-name"))
                                .withStream(
                                    new ViewStreamModel()
                                        .withName("stream-name")
                                        .withAttributeKeys(null)))),
                spiHelper,
                closeables)
            .build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }
}
