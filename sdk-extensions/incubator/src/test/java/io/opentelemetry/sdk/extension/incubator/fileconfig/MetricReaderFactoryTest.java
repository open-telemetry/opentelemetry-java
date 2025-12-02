/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.CardinalityLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalPrometheusMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MetricReaderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(DeclarativeConfiguration.class.getName());

  private final DeclarativeConfigContext context =
      spy(
          new DeclarativeConfigContext(
              SpiHelper.create(MetricReaderFactoryTest.class.getClassLoader())));

  @Test
  void create_PeriodicNullExporter() {
    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(
                        new MetricReaderModel().withPeriodic(new PeriodicMetricReaderModel()),
                        context))
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

    MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPeriodic(
                        new PeriodicMetricReaderModel()
                            .withExporter(
                                new PushMetricExporterModel()
                                    .withOtlpHttp(new OtlpHttpMetricExporterModel()))),
                context);
    MetricReader reader = readerAndCardinalityLimits.getMetricReader();
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    assertThat(readerAndCardinalityLimits.getCardinalityLimitsSelector()).isNull();
  }

  @Test
  void create_PeriodicConfigured() {
    List<Closeable> closeables = new ArrayList<>();
    MetricReader expectedReader =
        io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                OtlpHttpMetricExporter.getDefault())
            .setInterval(Duration.ofMillis(1))
            .build();
    cleanup.addCloseable(expectedReader);

    MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPeriodic(
                        new PeriodicMetricReaderModel()
                            .withExporter(
                                new PushMetricExporterModel()
                                    .withOtlpHttp(new OtlpHttpMetricExporterModel()))
                            .withInterval(1)
                            .withCardinalityLimits(new CardinalityLimitsModel().withDefault(100))),
                context);
    MetricReader reader = readerAndCardinalityLimits.getMetricReader();
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    assertThat(
            readerAndCardinalityLimits
                .getCardinalityLimitsSelector()
                .getCardinalityLimit(InstrumentType.COUNTER))
        .isEqualTo(100);
  }

  @Test
  void create_PullPrometheusDefault() throws IOException {
    int port = randomAvailablePort();
    List<Closeable> closeables = new ArrayList<>();
    PrometheusHttpServer expectedReader = PrometheusHttpServer.builder().setPort(port).build();
    // Close the reader to avoid port conflict with the new instance created by MetricReaderFactory
    expectedReader.close();

    MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPull(
                        new PullMetricReaderModel()
                            .withExporter(
                                new PullMetricExporterModel()
                                    .withPrometheusDevelopment(
                                        new ExperimentalPrometheusMetricExporterModel()
                                            .withPort(port)))),
                context);
    MetricReader reader = readerAndCardinalityLimits.getMetricReader();
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    assertThat(readerAndCardinalityLimits.getCardinalityLimitsSelector()).isNull();
    // TODO(jack-berg): validate prometheus component provider was invoked with correct arguments
    verify(context).loadComponent(eq(MetricReader.class), eq("prometheus/development"), any());
  }

  @Test
  void create_PullPrometheusConfigured() throws IOException {
    int port = randomAvailablePort();

    List<Closeable> closeables = new ArrayList<>();
    PrometheusHttpServer expectedReader =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(port)
            .setAllowedResourceAttributesFilter(
                IncludeExcludePredicate.createPatternMatching(
                    singletonList("foo"), singletonList("bar")))
            .build();
    // Close the reader to avoid port conflict with the new instance created by MetricReaderFactory
    expectedReader.close();

    MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
        MetricReaderFactory.getInstance()
            .create(
                new MetricReaderModel()
                    .withPull(
                        new PullMetricReaderModel()
                            .withCardinalityLimits(new CardinalityLimitsModel().withDefault(100))
                            .withExporter(
                                new PullMetricExporterModel()
                                    .withPrometheusDevelopment(
                                        new ExperimentalPrometheusMetricExporterModel()
                                            .withHost("localhost")
                                            .withPort(port)
                                            .withWithResourceConstantLabels(
                                                new IncludeExcludeModel()
                                                    .withIncluded(singletonList("foo"))
                                                    .withExcluded(singletonList("bar")))
                                            .withWithoutScopeInfo(true)
                                            .withWithoutTypeSuffix(true)
                                            .withWithoutUnits(true)))),
                context);
    MetricReader reader = readerAndCardinalityLimits.getMetricReader();
    cleanup.addCloseable(reader);
    cleanup.addCloseables(closeables);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    assertThat(
            readerAndCardinalityLimits
                .getCardinalityLimitsSelector()
                .getCardinalityLimit(InstrumentType.COUNTER))
        .isEqualTo(100);
    // TODO(jack-berg): validate prometheus component provider was invoked with correct arguments
    verify(context).loadComponent(eq(MetricReader.class), eq("prometheus/development"), any());
  }

  @Test
  void create_InvalidPullReader() {
    assertThatThrownBy(
            () ->
                MetricReaderFactory.getInstance()
                    .create(new MetricReaderModel().withPull(new PullMetricReaderModel()), context))
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
                        context))
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
