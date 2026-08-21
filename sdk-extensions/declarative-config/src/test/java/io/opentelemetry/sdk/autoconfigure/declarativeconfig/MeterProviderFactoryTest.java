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
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExemplarFilterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfiguratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterMatcherAndConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.MeterProviderModelAccessor;
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
        Arguments.argumentSet(
            "defaults", new MeterProviderModel(), SdkMeterProvider.builder().build()),
        Arguments.argumentSet(
            "with reader view and meter configurator",
            MeterProviderModelAccessor.setMeterConfigurator(
                    new MeterProviderModel()
                        .setReaders(
                            Collections.singletonList(
                                new MetricReaderModel()
                                    .setPeriodic(
                                        new PeriodicMetricReaderModel()
                                            .setExporter(
                                                new PushMetricExporterModel()
                                                    .setOtlpHttp(
                                                        new OtlpHttpMetricExporterModel())))))
                        .setViews(
                            Collections.singletonList(
                                new ViewModel()
                                    .setSelector(
                                        new ViewSelectorModel()
                                            .setInstrumentName("instrument-name"))
                                    .setStream(
                                        new ViewStreamModel()
                                            .setName("stream-name")
                                            .setAttributeKeys(null)))),
                    new ExperimentalMeterConfiguratorModel()
                        .setDefaultConfig(new ExperimentalMeterConfigModel().setEnabled(false))
                        .setMeters(
                            Collections.singletonList(
                                new ExperimentalMeterMatcherAndConfigModel()
                                    .setName("foo")
                                    .setConfig(
                                        new ExperimentalMeterConfigModel().setEnabled(true)))))
                .setExemplarFilter(ExemplarFilterModel.ALWAYS_ON),
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
