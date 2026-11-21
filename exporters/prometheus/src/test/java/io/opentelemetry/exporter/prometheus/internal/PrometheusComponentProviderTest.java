/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.config.DeclarativeConfigException;
import io.opentelemetry.api.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class PrometheusComponentProviderTest {

  private final PrometheusComponentProvider provider = new PrometheusComponentProvider();

  @Test
  void create_noResourceConstantLabels() throws IOException {
    try (MetricReader reader = provider.create(config(null))) {
      assertThat(reader.toString()).contains("allowedResourceAttributesFilter=null");
    }
  }

  @Test
  void create_emptyResourceConstantLabels() throws IOException {
    // resource_constant_labels: {} leaves both included and excluded unset. The prometheus exporter
    // specification requires that no resource attributes are added by default.
    DeclarativeConfigProperties config = config(DeclarativeConfigProperties.empty());

    try (MetricReader reader = provider.create(config)) {
      assertThat(reader.toString()).contains("allowedResourceAttributesFilter=null");
    }
  }

  @Test
  void create_includedResourceConstantLabels() throws IOException {
    DeclarativeConfigProperties config =
        config(resourceConstantLabels(singletonList("service.*"), null));

    try (MetricReader reader = provider.create(config)) {
      assertThat(reader.toString())
          .contains(
              "allowedResourceAttributesFilter=IncludeExcludePredicate{"
                  + "globMatchingEnabled=true, included=[service.*]}");
    }
  }

  @Test
  void create_excludedResourceConstantLabels() throws IOException {
    DeclarativeConfigProperties config =
        config(resourceConstantLabels(null, singletonList("service.*")));

    try (MetricReader reader = provider.create(config)) {
      assertThat(reader.toString())
          .contains(
              "allowedResourceAttributesFilter=IncludeExcludePredicate{"
                  + "globMatchingEnabled=true, excluded=[service.*]}");
    }
  }

  @Test
  void create_emptyIncludedList() {
    DeclarativeConfigProperties config = config(resourceConstantLabels(emptyList(), null));

    assertThatExceptionOfType(DeclarativeConfigException.class)
        .isThrownBy(() -> provider.create(config))
        .withMessage("included must not be empty");
  }

  @Test
  void create_emptyExcludedList() {
    DeclarativeConfigProperties config = config(resourceConstantLabels(null, emptyList()));

    assertThatExceptionOfType(DeclarativeConfigException.class)
        .isThrownBy(() -> provider.create(config))
        .withMessage("excluded must not be empty");
  }

  /** Returns a config which binds the prometheus server to an ephemeral port. */
  private static DeclarativeConfigProperties config(
      @Nullable DeclarativeConfigProperties resourceConstantLabels) {
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(0);
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceConstantLabels);
    return config;
  }

  private static DeclarativeConfigProperties resourceConstantLabels(
      @Nullable List<String> included, @Nullable List<String> excluded) {
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    when(config.getScalarList("included", String.class)).thenReturn(included);
    when(config.getScalarList("excluded", String.class)).thenReturn(excluded);
    return config;
  }
}
