/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

class HostResourceTest {
  @Test
  void shouldCreateRuntimeAttributes() {
    // when
    Resource resource = HostResource.buildResource();
    Attributes attributes = resource.getAttributes();

    // then
    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
    assertThat(attributes.get(ResourceAttributes.HOST_NAME)).isNotBlank();
    assertThat(attributes.get(ResourceAttributes.HOST_ARCH)).isNotBlank();
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @ExtendWith(SecurityManagerExtension.class)
  static class SecurityManagerEnabled {
    @Test
    void empty() {
      Attributes attributes = HostResource.buildResource().getAttributes();
      assertThat(attributes.asMap()).containsOnlyKeys(ResourceAttributes.HOST_NAME);
    }
  }
}
