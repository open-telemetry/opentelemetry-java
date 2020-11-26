/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.PropagationComponent;
import io.opencensus.trace.propagation.TextFormat;

public class OpenTelemetryPropagationComponentImpl extends PropagationComponent {

  private final BinaryFormat binaryFormat = new OpenTelemetryBinaryFormatImpl();
  private final TextFormat b3Format = new OpenTelemetryB3FormatImpl();
  private final TextFormat traceContextFormat = new OpenTelemetryTraceContextFormatImpl();

  @Override
  public BinaryFormat getBinaryFormat() {
    return binaryFormat;
  }

  @Override
  public TextFormat getB3Format() {
    return b3Format;
  }

  @Override
  public TextFormat getTraceContextFormat() {
    return traceContextFormat;
  }
}
