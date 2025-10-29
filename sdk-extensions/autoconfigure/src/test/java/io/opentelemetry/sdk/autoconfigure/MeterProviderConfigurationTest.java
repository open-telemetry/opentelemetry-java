/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.as;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOffExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOnExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.exemplar.TraceBasedExemplarFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

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
        .isInstanceOf(AlwaysOffExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "always_Off"))
        .isInstanceOf(AlwaysOffExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "always_on"))
        .isInstanceOf(AlwaysOnExemplarFilter.class);
    assertExemplarFilter(Collections.singletonMap("otel.metrics.exemplar.filter", "ALWAYS_ON"))
        .isInstanceOf(AlwaysOnExemplarFilter.class);
  }

  private static ObjectAssert<ExemplarFilterInternal> assertExemplarFilter(
      Map<String, String> config) {
    Map<String, String> configWithDefault = new HashMap<>(config);
    configWithDefault.put("otel.metrics.exporter", "none");
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        DefaultConfigProperties.createFromMap(configWithDefault),
        SpiHelper.create(MeterProviderConfigurationTest.class.getClassLoader()),
        (a, b) -> a,
        (a, b) -> a,
        new ArrayList<>());
    return assertThat(builder)
        .extracting(
            "exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilterInternal.class)));
  }
}
