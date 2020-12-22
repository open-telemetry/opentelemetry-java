/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ZPageServerTest {

  @Test
  void spanProcessor() {
    assertThat(ZPageServer.getSpanProcessor()).isInstanceOf(TracezSpanProcessor.class);
  }

  @Test
  void traceConfigSupplier() {
    assertThat(ZPageServer.getTracezTraceConfigSupplier())
        .isInstanceOf(TracezTraceConfigSupplier.class);
  }
}
