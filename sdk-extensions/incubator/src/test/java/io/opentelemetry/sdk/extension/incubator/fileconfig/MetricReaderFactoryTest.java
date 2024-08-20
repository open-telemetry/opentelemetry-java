/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
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
      LogCapturer.create().captureForLogger(FileConfiguration.class.getName());

  private SpiHelper spiHelper = SpiHelper.create(MetricReaderFactoryTest.class.getClassLoader());

  @Test
  void create_PeriodicNullExporter() {
    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReader().withPeriodic(new PeriodicMetricReader()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("periodic metric reader exporter is required but is null");
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
  void create_PullPrometheusDefault() throws IOException {
    int port = randomAvailablePort();
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    PrometheusHttpServer expectedReader = PrometheusHttpServer.builder().setPort(port).build();
    // Close the reader to avoid port conflict with the new instance created by MetricReaderFactory
    expectedReader.close();

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReader()
                    .withPull(
                        new PullMetricReader()
                            .withExporter(
                                new MetricExporter()
                                    .withPrometheus(new Prometheus().withPort(port)))),
                spiHelper,
                closeables);
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    // TODO(jack-berg): validate prometheus component provider was invoked with correct arguments
    verify(spiHelper).load(ComponentProvider.class);
  }

  @Test
  void create_PullPrometheusConfigured() throws IOException {
    int port = randomAvailablePort();

    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    PrometheusHttpServer expectedReader =
        PrometheusHttpServer.builder().setHost("localhost").setPort(port).build();
    // Close the reader to avoid port conflict with the new instance created by MetricReaderFactory
    expectedReader.close();

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReader()
                    .withPull(
                        new PullMetricReader()
                            .withExporter(
                                new MetricExporter()
                                    .withPrometheus(
                                        new Prometheus().withHost("localhost").withPort(port)))),
                spiHelper,
                closeables);
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    // TODO(jack-berg): validate prometheus component provider was invoked with correct arguments
    verify(spiHelper).load(ComponentProvider.class);
  }

  @Test
  void create_InvalidPullReader() {
    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReader().withPull(new PullMetricReader()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("pull metric reader exporter is required but is null");

    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReader()
                            .withPull(new PullMetricReader().withExporter(new MetricExporter())),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("prometheus is the only currently supported pull reader");

    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReader()
                            .withPull(
                                new PullMetricReader()
                                    .withExporter(new MetricExporter().withOtlp(new OtlpMetric()))),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("prometheus is the only currently supported pull reader");
  }

  /**
   * Find a random unused port. There's a small race if another process takes it before we
   * initialize. Consider adding retries to this test if it flakes, presumably it never will on CI
   * since there's no prometheus there blocking the well-known port.
   */
  private static int randomAvailablePort() throws IOException {
    try (ServerSocket socket2 = new ServerSocket(0)) {
      return socket2.getLocalPort();
    }
  }
}
