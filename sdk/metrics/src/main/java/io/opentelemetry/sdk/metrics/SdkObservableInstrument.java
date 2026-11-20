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
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import java.util.concurrent.atomic.AtomicReference;
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
  private final AtomicReference<Registration> registration;
  private final String callbackDescription;

  SdkObservableInstrument(SdkMeter sdkMeter, CallbackRegistration callbackRegistration) {
    this.registration = new AtomicReference<>(new Registration(sdkMeter, callbackRegistration));
    this.callbackDescription = callbackRegistration.toString();
  }

  @Override
  public void close() {
    Registration removed = registration.getAndSet(null);
    if (removed == null) {
      throttlingLogger.log(
          Level.WARNING, callbackDescription + " has called close() multiple times.");
      return;
    }
    removed.sdkMeter.removeCallback(removed.callbackRegistration);
  }

  @Override
  public String toString() {
    return "SdkObservableInstrument{callback=" + callbackDescription + "}";
  }

  private static final class Registration {
    private final SdkMeter sdkMeter;
    private final CallbackRegistration callbackRegistration;

    private Registration(SdkMeter sdkMeter, CallbackRegistration callbackRegistration) {
      this.sdkMeter = sdkMeter;
      this.callbackRegistration = callbackRegistration;
    }
  }
}
