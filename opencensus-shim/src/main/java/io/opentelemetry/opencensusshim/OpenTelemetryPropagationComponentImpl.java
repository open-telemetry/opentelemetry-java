/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.implcore.trace.propagation.PropagationComponentImpl;
import io.opencensus.trace.propagation.TextFormat;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;

class OpenTelemetryPropagationComponentImpl extends PropagationComponentImpl {

  private final TextFormat b3Format =
      new OpenTelemetryTextFormatImpl(B3Propagator.builder().injectMultipleHeaders().build());
  private final TextFormat traceContextFormat =
      new OpenTelemetryTextFormatImpl(W3CTraceContextPropagator.getInstance());

  @Override
  public TextFormat getB3Format() {
    return b3Format;
  }

  @Override
  public TextFormat getTraceContextFormat() {
    return traceContextFormat;
  }
}
