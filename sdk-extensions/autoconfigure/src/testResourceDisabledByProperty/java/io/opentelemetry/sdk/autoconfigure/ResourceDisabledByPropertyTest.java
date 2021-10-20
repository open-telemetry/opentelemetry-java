/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.v1.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Using class that will be made package-private
class ResourceDisabledByPropertyTest {

  @Test
  void osAndProcessDisabled() {
    Resource resource = OpenTelemetryResourceAutoConfiguration.configureResource();

    assertThat(resource.getAttribute(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(resource.getAttribute(ResourceAttributes.PROCESS_PID)).isNull();
    assertThat(resource.getAttribute(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
  }
}
