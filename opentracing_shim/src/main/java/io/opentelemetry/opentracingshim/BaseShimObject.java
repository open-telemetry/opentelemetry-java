/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.trace.Tracer;

abstract class BaseShimObject {
  final TelemetryInfo telemetryInfo;

  BaseShimObject(TelemetryInfo telemetryInfo) {
    this.telemetryInfo = telemetryInfo;
  }

  TelemetryInfo telemetryInfo() {
    return telemetryInfo;
  }

  Tracer tracer() {
    return telemetryInfo.tracer();
  }

  BaggageManager contextManager() {
    return telemetryInfo.contextManager();
  }

  SpanContextShimTable spanContextTable() {
    return telemetryInfo.spanContextTable();
  }

  ContextPropagators propagators() {
    return telemetryInfo.propagators();
  }
}
