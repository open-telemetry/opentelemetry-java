/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.metrics;

import io.opentelemetry.sdk.trace.samplers.SamplingResult;

class NoopSpanMetrics implements SpanMetrics {

  static final NoopSpanMetrics INSTANCE = new NoopSpanMetrics();

  @Override
  public SpanMetrics.Recording recordSpanStart(SamplingResult samplingResult) {
    return NoopRecording.INSTANCE;
  }

  private static class NoopRecording implements SpanMetrics.Recording {

    private static final NoopRecording INSTANCE = new NoopRecording();

    @Override
    public void recordSpanEnd() {}
  }
}
