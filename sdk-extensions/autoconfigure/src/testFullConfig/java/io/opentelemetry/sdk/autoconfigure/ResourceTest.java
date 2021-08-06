/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceTest {

  @Test
  void resource() {
    // make sure that the SDK is autoconfigured first regardless of test execution order
    GlobalOpenTelemetry.get();

    Attributes attributes = OpenTelemetryResourceAutoConfiguration.getResource().getAttributes();

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
