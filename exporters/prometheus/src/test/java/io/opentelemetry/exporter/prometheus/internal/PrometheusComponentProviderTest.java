/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrometheusComponentProviderTest {

  private final PrometheusComponentProvider provider = new PrometheusComponentProvider();

  @Test
  void getType() {
    assertThat(provider.getType()).isEqualTo(MetricReader.class);
  }

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("prometheus/development");
  }

  @Test
  void create_Default() throws IOException {
    int port = randomAvailablePort();
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(port);

    try (MetricReader metricReader = provider.create(config)) {
      assertThat(metricReader.toString()).contains("allowedResourceAttributesFilter=null");
    }
  }

  @Test
  void create_EmptyResourceConstantLabels() throws IOException {
    int port = randomAvailablePort();
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(port);
    when(config.getStructured("resource_constant_labels"))
        .thenReturn(DeclarativeConfigProperties.empty());

    try (MetricReader metricReader = provider.create(config)) {
      assertThat(metricReader.toString()).contains("allowedResourceAttributesFilter=null");
    }
  }

  @Test
  void create_IncludedResourceConstantLabels() throws IOException {
    int port = randomAvailablePort();
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    DeclarativeConfigProperties resourceLabels = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(port);
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceLabels);
    when(resourceLabels.getScalarList("included", String.class))
        .thenReturn(singletonList("service.name"));

    try (MetricReader metricReader = provider.create(config)) {
      assertThat(metricReader.toString())
          .contains(
              "allowedResourceAttributesFilter=IncludeExcludePredicate{globMatchingEnabled=true,"
                  + " included=[service.name]}");
    }
  }

  @Test
  void create_ExcludedResourceConstantLabels() throws IOException {
    int port = randomAvailablePort();
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    DeclarativeConfigProperties resourceLabels = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(port);
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceLabels);
    when(resourceLabels.getScalarList("excluded", String.class))
        .thenReturn(singletonList("host.name"));

    try (MetricReader metricReader = provider.create(config)) {
      assertThat(metricReader.toString())
          .contains(
              "allowedResourceAttributesFilter=IncludeExcludePredicate{globMatchingEnabled=true,"
                  + " excluded=[host.name]}");
    }
  }

  @Test
  void create_IncludedAndExcludedResourceConstantLabels() throws IOException {
    int port = randomAvailablePort();
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    DeclarativeConfigProperties resourceLabels = spy(DeclarativeConfigProperties.empty());
    when(config.getInt("port")).thenReturn(port);
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceLabels);
    when(resourceLabels.getScalarList("included", String.class))
        .thenReturn(Arrays.asList("k8s.*", "service.*"));
    when(resourceLabels.getScalarList("excluded", String.class))
        .thenReturn(singletonList("k8s.pod.uid"));

    try (MetricReader metricReader = provider.create(config)) {
      assertThat(metricReader.toString())
          .contains(
              "allowedResourceAttributesFilter=IncludeExcludePredicate{globMatchingEnabled=true,"
                  + " included=[k8s.*, service.*], excluded=[k8s.pod.uid]}");
    }
  }

  @Test
  void create_EmptyIncludedList_ThrowsException() {
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    DeclarativeConfigProperties resourceLabels = spy(DeclarativeConfigProperties.empty());
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceLabels);
    when(resourceLabels.getScalarList("included", String.class)).thenReturn(emptyList());

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("included must not be empty");
  }

  @Test
  void create_EmptyExcludedList_ThrowsException() {
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    DeclarativeConfigProperties resourceLabels = spy(DeclarativeConfigProperties.empty());
    when(config.getStructured("resource_constant_labels")).thenReturn(resourceLabels);
    when(resourceLabels.getScalarList("excluded", String.class)).thenReturn(emptyList());

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("excluded must not be empty");
  }

  @Test
  void create_InvalidTranslationStrategy_ThrowsException() {
    DeclarativeConfigProperties config = spy(DeclarativeConfigProperties.empty());
    when(config.getString("translation_strategy")).thenReturn("invalid_strategy");

    assertThatThrownBy(() -> provider.create(config))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("Unsupported translation_strategy: invalid_strategy");
  }

  private static int randomAvailablePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
