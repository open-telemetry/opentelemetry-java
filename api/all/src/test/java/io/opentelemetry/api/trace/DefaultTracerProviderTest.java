/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultTracerProviderTest {

  @Test
  void returnsDefaultTracer() {
    assertThat(TracerProvider.noop().get("test")).isInstanceOf(DefaultTracer.class);
    assertThat(TracerProvider.noop().get("test", "1.0")).isInstanceOf(DefaultTracer.class);
  }
}
