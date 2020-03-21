/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
