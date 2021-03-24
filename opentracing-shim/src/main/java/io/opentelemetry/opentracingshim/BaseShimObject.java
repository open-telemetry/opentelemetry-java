/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.trace.Tracer;

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

  SpanContextShimTable spanContextTable() {
    return telemetryInfo.spanContextTable();
  }

  Propagators propagators() {
    return telemetryInfo.propagators();
  }
}
