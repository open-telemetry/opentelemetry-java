/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AutoConfiguredOpenTelemetrySdkBuilderTest {

  @Test
  void getGlobalOpenTelemetryLock_findsLock() {
    assertThat(AutoConfiguredOpenTelemetrySdkBuilder.getGlobalOpenTelemetryLock()).isNotNull();
  }
}
