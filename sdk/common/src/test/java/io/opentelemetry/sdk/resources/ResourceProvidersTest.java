/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceProvidersTest {

  @Test
  void default_resource_includes_attributes_from_providers() {
    Resource resource = Resource.getDefault();

    long providerAttribute = resource.getAttributes().get(longKey("providerAttribute"));
    assertThat(providerAttribute).isNotNull();
    assertThat(providerAttribute).isEqualTo(42);

    assertThat(resource.getAttributes().get(ResourceAttributes.OS_TYPE)).isNotNull();
    assertThat(resource.getAttributes().get(ResourceAttributes.PROCESS_PID)).isNotNull();
    assertThat(resource.getAttributes().get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
  }
}
