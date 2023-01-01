/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.ObservableDoubleCounter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleUpDownCounter;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

class SdkObservableInstrument
    implements ObservableDoubleCounter,
        ObservableLongCounter,
        ObservableDoubleGauge,
        ObservableLongGauge,
        ObservableDoubleUpDownCounter,
        ObservableLongUpDownCounter,
        BatchCallback {

  private static final Logger logger = Logger.getLogger(SdkObservableInstrument.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final MeterSharedState meterSharedState;
  private final CallbackRegistration callbackRegistration;
  private final AtomicBoolean removed = new AtomicBoolean(false);

  SdkObservableInstrument(
      MeterSharedState meterSharedState, CallbackRegistration callbackRegistration) {
    this.meterSharedState = meterSharedState;
    this.callbackRegistration = callbackRegistration;
  }

  @Override
  public void close() {
    if (!removed.compareAndSet(false, true)) {
      throttlingLogger.log(
          Level.WARNING, callbackRegistration + " has called close() multiple times.");
      return;
    }
    meterSharedState.removeCallback(callbackRegistration);
  }

  @Override
  public String toString() {
    return "SdkObservableInstrument{callback=" + callbackRegistration + "}";
  }
}
