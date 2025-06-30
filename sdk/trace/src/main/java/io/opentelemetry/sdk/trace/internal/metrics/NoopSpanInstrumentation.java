/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.metrics;

import io.opentelemetry.sdk.trace.samplers.SamplingResult;

class NoopSpanInstrumentation implements SpanInstrumentation {

  static final NoopSpanInstrumentation INSTANCE = new NoopSpanInstrumentation();

  @Override
  public SpanInstrumentation.Recording recordSpanStart(SamplingResult samplingResult) {
    return NoopRecording.INSTANCE;
  }

  private static class NoopRecording implements SpanInstrumentation.Recording {

    private static final NoopRecording INSTANCE = new NoopRecording();

    @Override
    public void recordSpanEnd() {}
  }
}
