/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.provider.FirstResourceProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConditionalResourceProviderTest {

  @ParameterizedTest
  @CsvSource({
    "false, test-service-2, 0",
    "true, test-service, 1",
  })
  void shouldConditionallyProvideResourceAttributes_skipBasedOnConfig(
      boolean skipSecond, String expectedServiceName, int expectedCallsToFirst) {
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(
                () -> singletonMap("skip-second-resource-provider", String.valueOf(skipSecond)))
            .build();

    assertThat(sdk.getResource().getAttributes().asMap())
        .containsEntry(FirstResourceProvider.KEY, expectedServiceName);

    assertThat(FirstResourceProvider.calls).isEqualTo(expectedCallsToFirst);
  }
}
