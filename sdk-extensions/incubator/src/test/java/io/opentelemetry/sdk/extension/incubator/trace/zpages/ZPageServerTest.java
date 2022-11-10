/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ZPageServerTest {

  @Test
  void spanProcessor() {
    ZPageServer zPageServer = ZPageServer.create();
    assertThat(zPageServer.getSpanProcessor()).isInstanceOf(TracezSpanProcessor.class);
  }

  @Test
  void traceConfigSupplier() {
    ZPageServer zPageServer = ZPageServer.create();
    assertThat(zPageServer.getTracezTraceConfigSupplier())
        .isInstanceOf(TracezTraceConfigSupplier.class);
  }

  @Test
  void testSampler() {
    ZPageServer zPageServer = ZPageServer.create();
    assertThat(zPageServer.getTracezSampler()).isInstanceOf(TracezTraceConfigSupplier.class);
  }
}
