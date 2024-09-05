/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

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
