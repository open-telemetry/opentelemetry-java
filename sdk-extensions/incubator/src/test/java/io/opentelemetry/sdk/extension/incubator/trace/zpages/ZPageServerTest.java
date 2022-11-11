/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import org.junit.jupiter.api.Test;

class ZPageServerTest {

  @Test
  void spanProcessor() {
    ZPageServer server = ZPageServer.create();
    assertThat(server.getSpanProcessor()).isInstanceOf(TracezSpanProcessor.class);
  }

  @Test
  void traceConfigSupplier() {
    ZPageServer server = ZPageServer.create();
    assertThat(server.getTracezTraceConfigSupplier()).isInstanceOf(TracezTraceConfigSupplier.class);
  }

  @Test
  void testSampler() {
    ZPageServer server = ZPageServer.create();
    assertThat(server.getTracezSampler()).isInstanceOf(TracezTraceConfigSupplier.class);
  }

  @Test
  void buildTracerProvider() {
    ZPageServer server = ZPageServer.create();
    SpanLimits expectedLimits = server.getTracezTraceConfigSupplier().get();

    try (SdkTracerProvider provider = server.buildSdkTracerProvider()) {
      assertThat(provider.getSpanLimits()).isEqualTo(expectedLimits);
      assertThat(provider.getSampler()).isSameAs(server.getTracezSampler());
    }
  }
}
