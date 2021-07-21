/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ResourceTest {

  @Test
  void noResourceProviders() {
    assertThat(OpenTelemetrySdkAutoConfiguration.getResource())
        .isEqualTo(
            Resource.getDefault().toBuilder().setSchemaUrl(ResourceAttributes.SCHEMA_URL).build());
  }
}
