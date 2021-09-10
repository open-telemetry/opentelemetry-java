/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceTest {
  @Mock ConfigProperties config;

  @Test
  void noResourceProviders() {
    assertThat(OpenTelemetryResourceAutoConfiguration.configureResource())
        .isEqualTo(
            Resource.getDefault().toBuilder().setSchemaUrl(ResourceAttributes.SCHEMA_URL).build());
  }

  @Test
  void customConfigResource() {
    when(config.getString("otel.service.name")).thenReturn("test-service");
    when(config.getMap("otel.resource.attributes")).thenReturn(singletonMap("food", "cheesecake"));

    assertThat(OpenTelemetryResourceAutoConfiguration.configureResource(config))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "test-service")
                .put("food", "cheesecake")
                .setSchemaUrl(ResourceAttributes.SCHEMA_URL)
                .build());
  }
}
