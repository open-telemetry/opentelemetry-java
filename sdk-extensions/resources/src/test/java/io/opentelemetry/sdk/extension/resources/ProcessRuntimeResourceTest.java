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

class ProcessRuntimeResourceTest {
  @Test
  void shouldCreateRuntimeAttributes() {
    // when
    Attributes attributes = ProcessRuntimeResource.get().getAttributes();

    // then
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotBlank();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_VERSION)).isNotBlank();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_DESCRIPTION)).isNotBlank();
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @ExtendWith(SecurityManagerExtension.class)
  static class SecurityManagerEnabled {
    @Test
    void empty() {
      assertThat(ProcessRuntimeResource.buildResource()).isEqualTo(Resource.empty());
    }
  }
}
