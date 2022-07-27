/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

class OrderedSpiTest {

  @Test
  void shouldLoadSpiImplementationsInOrder() {
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .registerShutdownHook(false)
            .build();

    assertThat(sdk.getResource().getAttributes().asMap())
        .contains(
            entry(stringKey("otel.some_resource"), "real value"),
            entry(stringKey("otel.from_config"), "configured value"));
  }
}
