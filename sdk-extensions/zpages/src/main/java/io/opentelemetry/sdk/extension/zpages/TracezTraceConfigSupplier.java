/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.util.function.Supplier;

final class TracezTraceConfigSupplier implements Supplier<TraceConfig> {

  private volatile TraceConfig activeTraceConfig;

  TracezTraceConfigSupplier() {
    activeTraceConfig = TraceConfig.getDefault();
  }

  @Override
  public TraceConfig get() {
    return activeTraceConfig;
  }

  TraceConfig getActiveTraceConfig() {
    return activeTraceConfig;
  }

  void setActiveTraceConfig(TraceConfig traceConfig) {
    activeTraceConfig = traceConfig;
  }
}
