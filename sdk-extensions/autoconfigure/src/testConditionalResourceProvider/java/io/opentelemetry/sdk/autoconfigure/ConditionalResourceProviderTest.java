/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

class ConditionalResourceProviderTest {

  @Test
  void shouldConditionallyProvideResourceAttributes_skipBasedOnPreviousResource() {
    AutoConfiguredOpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.builder().build();

    // ServiceInstanceIdResourceProvider now runs at Integer.MIN_VALUE and always adds
    // service.instance.id, causing FirstResourceProvider (a ConditionalResourceProvider) to skip.
    // This leaves the default service.name from the test environment (test-service-2).
    assertThat(sdk.getResource().getAttributes().asMap())
        .contains(entry(stringKey("service.name"), "test-service-2"));
  }

  @Test
  void shouldConditionallyProvideResourceAttributes_skipBasedOnConfig() {
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(() -> singletonMap("skip-first-resource-provider", "true"))
            .build();

    assertThat(sdk.getResource().getAttributes().asMap())
        .contains(entry(stringKey("service.name"), "test-service-2"));
  }
}
