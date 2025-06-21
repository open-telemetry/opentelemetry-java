/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.metrics;

import io.opentelemetry.sdk.trace.samplers.SamplingResult;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public interface SpanMetrics {

  static SpanMetrics noop() {
    return NoopSpanMetrics.INSTANCE;
  }

  Recording recordSpanStart(SamplingResult samplingResult);

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  interface Recording {

    void recordSpanEnd();
  }
}
