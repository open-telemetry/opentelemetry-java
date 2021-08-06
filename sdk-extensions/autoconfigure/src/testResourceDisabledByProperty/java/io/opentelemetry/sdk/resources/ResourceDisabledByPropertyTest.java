/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.OpenTelemetryResourceAutoConfiguration;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceDisabledByPropertyTest {

  @Test
  void osAndProcessDisabled() {
    Resource resource = OpenTelemetryResourceAutoConfiguration.initialize();

    assertThat(resource.getAttributes().get(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(resource.getAttributes().get(ResourceAttributes.PROCESS_PID)).isNull();
    assertThat(resource.getAttributes().get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
  }
}
