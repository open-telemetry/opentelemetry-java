/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.testing.internal.AbstractDefaultTracerTest;
import org.junit.jupiter.api.Disabled;

@Disabled
class DefaultTracerTest extends AbstractDefaultTracerTest {

  @Override
  public Tracer getTracer() {
    return DefaultTracer.getInstance();
  }

  @Override
  public TracerProvider getTracerProvider() {
    return DefaultTracerProvider.getInstance();
  }
}
