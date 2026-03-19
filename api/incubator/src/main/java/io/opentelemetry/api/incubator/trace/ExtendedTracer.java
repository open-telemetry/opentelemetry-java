/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;

/** Extended {@link Tracer} with experimental APIs. */
public interface ExtendedTracer extends Tracer {

  @Override
  ExtendedSpanBuilder spanBuilder(String spanName);
}
