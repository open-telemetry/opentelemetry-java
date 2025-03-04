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
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetricModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PrometheusModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
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
      LogCapturer.create().captureForLogger(DeclarativeConfiguration.class.getName());

  private SpiHelper spiHelper = SpiHelper.create(MetricReaderFactoryTest.class.getClassLoader());

  @Test
  void create_PeriodicNullExporter() {
    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReaderModel().withPeriodic(new PeriodicMetricReaderModel()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("periodic metric reader exporter is required but is null");
  }

  @Test
  void create_PeriodicDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.metrics.export.PeriodicMetricReader expectedReader =
        io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                OtlpHttpMetricExporter.getDefault())
            .build();
    cleanup.addCloseable(expectedReader);

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPeriodic(
                        new PeriodicMetricReaderModel()
                            .withExporter(
                                new PushMetricExporterModel().withOtlp(new OtlpMetricModel()))),
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
                OtlpHttpMetricExporter.getDefault())
            .setInterval(Duration.ofMillis(1))
            .build();
    cleanup.addCloseable(expectedReader);

    io.opentelemetry.sdk.metrics.export.MetricReader reader =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPeriodic(
                        new PeriodicMetricReaderModel()
                            .withExporter(
                                new PushMetricExporterModel().withOtlp(new OtlpMetricModel()))
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
                new MetricReaderModel()
                    .withPull(
                        new PullMetricReaderModel()
                            .withExporter(
                                new PullMetricExporterModel()
                                    .withPrometheus(new PrometheusModel().withPort(port)))),
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
                new MetricReaderModel()
                    .withPull(
                        new PullMetricReaderModel()
                            .withExporter(
                                new PullMetricExporterModel()
                                    .withPrometheus(
                                        new PrometheusModel()
                                            .withHost("localhost")
                                            .withPort(port)))),
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
                        new MetricReaderModel().withPull(new PullMetricReaderModel()),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("pull metric reader exporter is required but is null");

    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReaderModel()
                            .withPull(
                                new PullMetricReaderModel()
                                    .withExporter(new PullMetricExporterModel())),
                        spiHelper,
                        Collections.emptyList()))
        .isInstanceOf(DeclarativeConfigException.class)
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
