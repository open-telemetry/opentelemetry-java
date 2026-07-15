/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.TranslationStrategy;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalPrometheusMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalPrometheusTranslationStrategyModel;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MetricReaderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      spy(
          new DeclarativeConfigContext(
              ComponentLoader.forClassLoader(MetricReaderFactoryTest.class.getClassLoader())));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    clearInvocations(context);
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(
      MetricReaderModel model,
      MetricReader expectedReader,
      Integer expectedDefaultCardinalityLimit,
      boolean verifyLoadComponent) {
    cleanup.addCloseable(expectedReader);
    MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
        MetricReaderFactory.getInstance().create(model, context);
    MetricReader reader = readerAndCardinalityLimits.getMetricReader();
    cleanup.addCloseable(reader);

    assertThat(reader.toString()).isEqualTo(expectedReader.toString());
    if (expectedDefaultCardinalityLimit == null) {
      assertThat(readerAndCardinalityLimits.getCardinalityLimitsSelector()).isNull();
    } else {
      assertThat(
              readerAndCardinalityLimits
                  .getCardinalityLimitsSelector()
                  .getCardinalityLimit(InstrumentType.COUNTER))
          .isEqualTo(expectedDefaultCardinalityLimit);
    }
    if (verifyLoadComponent) {
      verify(context).loadComponent(eq(MetricReader.class), any(ConfigKeyValue.class));
    }
  }

  /**
   * Find a random unused port. There's a small race if another process takes it before we
   * initialize. Consider adding retries to this test if it flakes, presumably it never will on CI
   * since there's no prometheus there blocking the well-known port.
   */
  static Stream<Arguments> createTestCases() throws IOException {
    List<Arguments> testCases =
        new ArrayList<>(
            Arrays.asList(
                Arguments.argumentSet(
                    "periodic defaults",
                    new MetricReaderModel()
                        .withPeriodic(
                            new PeriodicMetricReaderModel()
                                .withExporter(
                                    new PushMetricExporterModel()
                                        .withOtlpHttp(new OtlpHttpMetricExporterModel()))),
                    PeriodicMetricReader.builder(
                            OtlpHttpMetricExporter.builder().setComponentLoader(context).build())
                        .build(),
                    null,
                    false),
                Arguments.argumentSet(
                    "periodic configured",
                    new MetricReaderModel()
                        .withPeriodic(
                            new PeriodicMetricReaderModel()
                                .withExporter(
                                    new PushMetricExporterModel()
                                        .withOtlpHttp(new OtlpHttpMetricExporterModel()))
                                .withInterval(1)
                                .withCardinalityLimits(
                                    new CardinalityLimitsModel().withDefault(100))
                                .withMaxExportBatchSizeDevelopment(200)),
                    SdkMeterProviderUtil.setMaxExportBatchSize(
                            PeriodicMetricReader.builder(
                                    OtlpHttpMetricExporter.builder()
                                        .setComponentLoader(context)
                                        .build())
                                .setInterval(Duration.ofMillis(1)),
                            200)
                        .build(),
                    100,
                    false)));

    int prom1Port = randomAvailablePort();
    PrometheusHttpServer prom1Expected = PrometheusHttpServer.builder().setPort(prom1Port).build();
    // Close immediately to release the port before MetricReaderFactory creates a server on it
    prom1Expected.close();
    testCases.add(
        Arguments.argumentSet(
            "pull prometheus defaults",
            new MetricReaderModel()
                .withPull(
                    new PullMetricReaderModel()
                        .withExporter(
                            new PullMetricExporterModel()
                                .withPrometheusDevelopment(
                                    new ExperimentalPrometheusMetricExporterModel()
                                        .withPort(prom1Port)))),
            prom1Expected,
            null,
            true));

    int prom2Port = randomAvailablePort();
    PrometheusHttpServer prom2Expected =
        PrometheusHttpServer.builder()
            .setHost("localhost")
            .setPort(prom2Port)
            .setTranslationStrategy(TranslationStrategy.UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES)
            .setAllowedResourceAttributesFilter(
                IncludeExcludePredicate.createPatternMatching(
                    singletonList("foo"), singletonList("bar")))
            .build();
    prom2Expected.close();
    // Close immediately to release the port before MetricReaderFactory creates a server on it
    testCases.add(
        Arguments.argumentSet(
            "pull prometheus configured",
            new MetricReaderModel()
                .withPull(
                    new PullMetricReaderModel()
                        .withCardinalityLimits(new CardinalityLimitsModel().withDefault(100))
                        .withExporter(
                            new PullMetricExporterModel()
                                .withPrometheusDevelopment(
                                    new ExperimentalPrometheusMetricExporterModel()
                                        .withHost("localhost")
                                        .withPort(prom2Port)
                                        .withResourceConstantLabels(
                                            new IncludeExcludeModel()
                                                .withIncluded(singletonList("foo"))
                                                .withExcluded(singletonList("bar")))
                                        .withScopeInfoEnabled(false)
                                        .withTargetInfoEnabledDevelopment(false)
                                        .withTranslationStrategy(
                                            ExperimentalPrometheusTranslationStrategyModel
                                                .UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES_DEVELOPMENT)))),
            prom2Expected,
            100,
            true));

    int prom3Port = randomAvailablePort();
    PrometheusHttpServer prom3Expected =
        PrometheusHttpServer.builder()
            .setPort(prom3Port)
            .setTranslationStrategy(TranslationStrategy.NO_TRANSLATION)
            .build();
    prom3Expected.close();
    // Close immediately to release the port before MetricReaderFactory creates a server on it
    testCases.add(
        Arguments.argumentSet(
            "pull prometheus no translation",
            new MetricReaderModel()
                .withPull(
                    new PullMetricReaderModel()
                        .withExporter(
                            new PullMetricExporterModel()
                                .withPrometheusDevelopment(
                                    new ExperimentalPrometheusMetricExporterModel()
                                        .withHost("localhost")
                                        .withPort(prom3Port)
                                        .withTranslationStrategy(
                                            ExperimentalPrometheusTranslationStrategyModel
                                                .NO_TRANSLATION_DEVELOPMENT)))),
            prom3Expected,
            null,
            true));

    return testCases.stream();
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(MetricReaderModel model, String expectedMessage) {
    assertThatThrownBy(() -> MetricReaderFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  static Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "null periodic reader exporter",
            new MetricReaderModel().withPeriodic(new PeriodicMetricReaderModel()),
            "periodic metric reader exporter is required but is null"),
        Arguments.argumentSet(
            "null pull reader",
            new MetricReaderModel().withPull(new PullMetricReaderModel()),
            "pull metric reader exporter is required but is null"),
        Arguments.argumentSet(
            "null pull reader exporter",
            new MetricReaderModel()
                .withPull(new PullMetricReaderModel().withExporter(new PullMetricExporterModel())),
            "metric reader must have exactly one entry but has 0"));
  }

  private static int randomAvailablePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
