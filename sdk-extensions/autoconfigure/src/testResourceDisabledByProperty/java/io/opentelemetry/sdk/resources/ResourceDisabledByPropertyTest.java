/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceDisabledByPropertyTest {

  @Test
  void osAndProcessDisabled() {
    Resource resource = OpenTelemetrySdkAutoConfiguration.builder().build().newResource();

    assertThat(resource.getAttribute(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(resource.getAttribute(ResourceAttributes.PROCESS_PID)).isNull();
    assertThat(resource.getAttribute(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
  }
}
