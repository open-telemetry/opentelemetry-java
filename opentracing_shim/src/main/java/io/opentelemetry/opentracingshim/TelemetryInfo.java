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

import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.trace.Tracer;

/**
 * Utility class that holds a Tracer, a BaggageManager, and related objects that are core part of
 * the OT Shim layer.
 */
final class TelemetryInfo {
  private final Tracer tracer;
  private final BaggageManager contextManager;
  private final Baggage emptyBaggage;
  private final ContextPropagators propagators;
  private final SpanContextShimTable spanContextTable;

  TelemetryInfo(Tracer tracer, BaggageManager contextManager, ContextPropagators propagators) {
    this.tracer = tracer;
    this.contextManager = contextManager;
    this.propagators = propagators;
    this.emptyBaggage = contextManager.baggageBuilder().build();
    this.spanContextTable = new SpanContextShimTable();
  }

  Tracer tracer() {
    return tracer;
  }

  BaggageManager contextManager() {
    return contextManager;
  }

  SpanContextShimTable spanContextTable() {
    return spanContextTable;
  }

  Baggage emptyBaggage() {
    return emptyBaggage;
  }

  ContextPropagators propagators() {
    return propagators;
  }
}
