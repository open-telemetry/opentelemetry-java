/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import org.junit.jupiter.api.Test;

class ResourceProvidersTest {

  @Test
  void default_resource_includes_attributes_from_providers() {
    Resource resource = Resource.getDefault();

    AttributeValue providerAttribute = resource.getAttributes().get("providerAttribute");
    assertThat(providerAttribute).isNotNull();
    assertThat(providerAttribute.getLongValue()).isEqualTo(42);
  }
}
