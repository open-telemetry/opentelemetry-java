/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.as;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOffFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOnFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.TraceBasedExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MeterProviderConfigurationTest {

  @Test
  void configureMeterProvider_ConfiguresExemplarFilter() {
    assertExemplarFilter(Collections.emptyMap()).isInstanceOf(TraceBasedExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "foo"))
        .isInstanceOf(TraceBasedExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "trace_based"))
        .isInstanceOf(TraceBasedExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "Trace_based"))
        .isInstanceOf(TraceBasedExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "always_off"))
        .isInstanceOf(AlwaysOffFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "always_Off"))
        .isInstanceOf(AlwaysOffFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "always_on"))
        .isInstanceOf(AlwaysOnFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "ALWAYS_ON"))
        .isInstanceOf(AlwaysOnFilter.class);
  }

  @Test
  void configureMeterProvider_ConfiguresCardinalityLimit() {
    assertCardinalityLimit(
            Collections.singletonMap("otel.java.experimental.metrics.cardinality.limit", "5"))
        .allMatch(el -> el == 5);
    assertCardinalityLimit(Collections.emptyMap())
        .allMatch(el -> el == MetricStorage.DEFAULT_MAX_CARDINALITY);
  }

  private static ObjectAssert<ExemplarFilter> assertExemplarFilter(Map<String, String> config) {
    Map<String, String> configWithDefault = new HashMap<>(config);
    configWithDefault.put("otel.metrics.exporter", "none");
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        DefaultConfigProperties.createForTest(configWithDefault),
        MeterProviderConfigurationTest.class.getClassLoader(),
        (a, b) -> a,
        new ArrayList<>());
    return assertThat(builder)
        .extracting("exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilter.class)));
  }

  @Test
  @SuppressWarnings({"unchecked"})
  private static ListAssert<Integer> assertCardinalityLimit(Map<String, String> config) {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name",
            "description",
            "unit",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG,
            Advice.empty());

    SdkMeterProvider meterProvider = setupMeterProviderMockedExporter(config);
    try {
      Field field = SdkMeterProvider.class.getDeclaredField("registeredReaders");
      field.setAccessible(true);
      List<RegisteredReader> readers = (List<RegisteredReader>) field.get(meterProvider);
      if (readers.isEmpty()) {
        return assertThat(Collections.emptyList());
      }
      RegisteredReader reader = readers.get(0);
      ViewRegistry registry = reader.getViewRegistry();
      List<RegisteredView> views = registry.findViews(descriptor, null);
      return assertThat(
          views.stream().map(view -> view.getCardinalityLimit()).collect(Collectors.toList()));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("Error accessing registeredReaders on SdkMeterProvider", e);
    }
  }

  private static SdkMeterProvider setupMeterProviderMockedExporter(Map<String, String> config) {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    ConfigProperties defaultConfig = DefaultConfigProperties.createForTest(config);
    ClassLoader serviceClassLoader = AutoConfiguredOpenTelemetrySdkBuilder.class.getClassLoader();
    try (MockedStatic<MetricExporterConfiguration> mockExporter =
        Mockito.mockStatic(MetricExporterConfiguration.class)) {
      mockExporter
          .when(
              () ->
                  MetricExporterConfiguration.configureReader(
                      "otlp", defaultConfig, serviceClassLoader, (a, b) -> a, new ArrayList<>()))
          .thenReturn(InMemoryMetricReader.create());

      MeterProviderConfiguration.configureMeterProvider(
          builder, defaultConfig, serviceClassLoader, (a, b) -> a, new ArrayList<>());

      return builder.build();
    }
  }
}
