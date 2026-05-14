/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil.setMeterConfigurator;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalMeterConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalMeterConfiguratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalMeterMatcherAndConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MeterProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(MeterProviderFactoryTest.class.getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(MeterProviderModel model, SdkMeterProvider expectedProvider) {
    List<Closeable> closeables = new ArrayList<>();
    cleanup.addCloseable(expectedProvider);

    SdkMeterProvider provider = MeterProviderFactory.getInstance().create(model, context).build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(new MeterProviderModel(), SdkMeterProvider.builder().build()),
        Arguments.of(
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
                        new ViewModel()
                            .withSelector(
                                new ViewSelectorModel().withInstrumentName("instrument-name"))
                            .withStream(
                                new ViewStreamModel()
                                    .withName("stream-name")
                                    .withAttributeKeys(null))))
                .withMeterConfiguratorDevelopment(
                    new ExperimentalMeterConfiguratorModel()
                        .withDefaultConfig(new ExperimentalMeterConfigModel().withEnabled(false))
                        .withMeters(
                            Collections.singletonList(
                                new ExperimentalMeterMatcherAndConfigModel()
                                    .withName("foo")
                                    .withConfig(
                                        new ExperimentalMeterConfigModel().withEnabled(true)))))
                .withExemplarFilter(MeterProviderModel.ExemplarFilter.ALWAYS_ON),
            setMeterConfigurator(
                    SdkMeterProvider.builder(),
                    ScopeConfigurator.<MeterConfig>builder()
                        .setDefault(MeterConfig.disabled())
                        .addCondition(
                            ScopeConfiguratorBuilder.nameMatchesGlob("foo"), MeterConfig.enabled())
                        .build())
                .setExemplarFilter(ExemplarFilter.alwaysOn())
                .registerMetricReader(
                    PeriodicMetricReader.builder(
                            OtlpHttpMetricExporter.builder().setComponentLoader(context).build())
                        .build())
                .registerView(
                    InstrumentSelector.builder().setName("instrument-name").build(),
                    View.builder().setName("stream-name").build())
                .build()));
  }
}
