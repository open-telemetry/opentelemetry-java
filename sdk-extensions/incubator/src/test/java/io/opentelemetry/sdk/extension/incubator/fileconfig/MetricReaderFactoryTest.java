/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MetricReaderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(ConfigurationFactory.class.getName());

  private final SpiHelper spiHelper =
      SpiHelper.create(MetricReaderFactoryTest.class.getClassLoader());

  @Test
  void create_Null() {
    assertThat(MetricReaderFactory.getInstance().create(null, spiHelper, Collections.emptyList()))
        .isNull();
  }

  @Test
  void create_PeriodicNullExporter() {
    assertThat(
            MetricReaderFactory.getInstance()
                .create(
                    new MetricReader().withPeriodic(new PeriodicMetricReader()),
                    spiHelper,
                    Collections.emptyList()))
        .isNull();
  }

  @Test
  void create_PeriodicDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.metrics.export.PeriodicMetricReader expectedReader =
        io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                OtlpGrpcMetricExporter.getDefault())
            .build();
    cleanup.addCloseable(expectedReader);

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReader()
                    .withPeriodic(
                        new PeriodicMetricReader()
                            .withExporter(new MetricExporter().withOtlp(new OtlpMetric()))),
                spiHelper,
                closeables);
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
  }

  @Test
  void create_PeriodicConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.metrics.export.MetricReader expectedReader =
        io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                OtlpGrpcMetricExporter.getDefault())
            .setInterval(Duration.ofMillis(1))
            .build();
    cleanup.addCloseable(expectedReader);

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReader()
                    .withPeriodic(
                        new PeriodicMetricReader()
                            .withExporter(new MetricExporter().withOtlp(new OtlpMetric()))
                            .withInterval(1)),
                spiHelper,
                closeables);
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
  }

  @Test
  @SuppressLogger(ConfigurationFactory.class)
  void create_Pull() {
    assertThat(
            MetricReaderFactory.getInstance()
                .create(
                    new MetricReader().withPull(new PullMetricReader()),
                    spiHelper,
                    Collections.emptyList()))
        .isNull();

    logCapturer.assertContains("pull metric reader not currently supported");
  }
}
