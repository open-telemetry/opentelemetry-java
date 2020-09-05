/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.trace.Tracer;

/**
 * Utility class that holds a Tracer, a CorrelationContextManager, and related objects that are core
 * part of the OT Shim layer.
 */
final class TelemetryInfo {
  private final Tracer tracer;
  private final CorrelationContextManager contextManager;
  private final CorrelationContext emptyCorrelationContext;
  private final ContextPropagators propagators;
  private final SpanContextShimTable spanContextTable;

  TelemetryInfo(
      Tracer tracer, CorrelationContextManager contextManager, ContextPropagators propagators) {
    this.tracer = tracer;
    this.contextManager = contextManager;
    this.propagators = propagators;
    this.emptyCorrelationContext = contextManager.contextBuilder().build();
    this.spanContextTable = new SpanContextShimTable();
  }

  Tracer tracer() {
    return tracer;
  }

  CorrelationContextManager contextManager() {
    return contextManager;
  }

  SpanContextShimTable spanContextTable() {
    return spanContextTable;
  }

  CorrelationContext emptyCorrelationContext() {
    return emptyCorrelationContext;
  }

  ContextPropagators propagators() {
    return propagators;
  }
}
