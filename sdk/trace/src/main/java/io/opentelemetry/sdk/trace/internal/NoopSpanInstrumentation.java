/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

class NoopSpanInstrumentation implements SpanInstrumentation {

  static final NoopSpanInstrumentation INSTANCE = new NoopSpanInstrumentation();

  static final SpanInstrumentation.Recording RECORDING_INSTANCE = new NoopRecording();

  @Override
  public SpanInstrumentation.Recording recordSpanStart(
      SamplingResult samplingResult, SpanContext parentContext) {
    return RECORDING_INSTANCE;
  }

  private static class NoopRecording implements SpanInstrumentation.Recording {

    @Override
    public boolean isNoop() {
      return true;
    }

    @Override
    public void recordSpanEnd() {}
  }
}
