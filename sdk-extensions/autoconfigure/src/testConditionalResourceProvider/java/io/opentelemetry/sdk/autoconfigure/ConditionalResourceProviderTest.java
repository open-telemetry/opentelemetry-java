/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ConditionalResourceProviderTest {

  @Test
  void shouldConditionallyProvideResourceAttributes_skipBasedOnPreviousResource() {
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .registerShutdownHook(false)
            .build();

    assertThat(sdk.getResource().getAttributes().asMap())
        .contains(entry(ResourceAttributes.SERVICE_NAME, "test-service"));
  }

  @Test
  void shouldConditionallyProvideResourceAttributes_skipBasedOnConfig() {
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .registerShutdownHook(false)
            .addPropertiesSupplier(() -> singletonMap("skip-first-resource-provider", "true"))
            .build();

    assertThat(sdk.getResource().getAttributes().asMap())
        .contains(entry(ResourceAttributes.SERVICE_NAME, "test-service-2"));
  }
}
