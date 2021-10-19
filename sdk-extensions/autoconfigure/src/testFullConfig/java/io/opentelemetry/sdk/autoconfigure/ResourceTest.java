/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceTest {

  @Test
  void resource() {
    Attributes attributes =
        OpenTelemetrySdkAutoConfiguration.builder().build().newResource().getAttributes();

    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE)).isNotNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_VERSION)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_DESCRIPTION)).isNotNull();
  }
}
