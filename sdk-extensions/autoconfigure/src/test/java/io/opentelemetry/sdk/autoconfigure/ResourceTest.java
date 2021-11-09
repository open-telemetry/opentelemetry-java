/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("deprecation") // Testing class which will be made package-private
@ExtendWith(MockitoExtension.class)
class ResourceTest {

  @Test
  void noResourceProviders() {
    assertThat(OpenTelemetryResourceAutoConfiguration.configureResource())
        .isEqualTo(
            Resource.getDefault().toBuilder().setSchemaUrl(ResourceAttributes.SCHEMA_URL).build());
  }

  @Test
  void customConfigResource() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put("otel.resource.attributes", "food=cheesecake");

    assertThat(
            OpenTelemetryResourceAutoConfiguration.configureResource(
                DefaultConfigProperties.get(props)))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "test-service")
                .put("food", "cheesecake")
                .setSchemaUrl(ResourceAttributes.SCHEMA_URL)
                .build());
  }
}
