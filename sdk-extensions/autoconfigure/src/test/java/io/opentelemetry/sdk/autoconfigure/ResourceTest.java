/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceTest {
  @Mock ConfigProperties config;

  @BeforeEach
  void setUpDefaultConfigValues() {
    // use defaults in most settings
    when(config.getString(anyString())).thenReturn(null);
    when(config.getInt(anyString())).thenReturn(null);
    // and disable exporters
    when(config.getString("otel.traces.exporter")).thenReturn("none");
    when(config.getString("otel.metrics.exporter")).thenReturn("none");
  }

  @Test
  void noResourceProviders() {
    // make sure resource gets initialized
    OpenTelemetrySdkAutoConfiguration.initialize(false, config);

    assertThat(OpenTelemetrySdkAutoConfiguration.getResource())
        .isEqualTo(
            Resource.getDefault().toBuilder().setSchemaUrl(ResourceAttributes.SCHEMA_URL).build());
  }
}
