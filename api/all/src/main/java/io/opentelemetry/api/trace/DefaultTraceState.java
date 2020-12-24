/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * Class to hold on to the default {@link TraceState} instance. This is needed to work around class
 * loading cycles.
 */
class DefaultTraceState {
  private static final TraceState DEFAULT = new ArrayBasedTraceStateBuilder().build();

  static TraceState get() {
    return DEFAULT;
  }

  private DefaultTraceState() {}
}
