/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.BatchCallbackBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkBatchCallbackBuilder implements BatchCallbackBuilder {

  private static final Logger logger = Logger.getLogger(SdkBatchCallbackBuilder.class.getName());

  private final MeterSharedState meterSharedState;
  private final List<SdkObservableMeasurement> observableMeasurements = new ArrayList<>();

  SdkBatchCallbackBuilder(MeterSharedState meterSharedState) {
    this.meterSharedState = meterSharedState;
  }

  @Override
  public BatchCallbackBuilder add(ObservableDoubleMeasurement... observables) {
    for (ObservableDoubleMeasurement observable : observables) {
      add(observable);
    }
    return this;
  }

  @Override
  public BatchCallbackBuilder add(ObservableLongMeasurement... observables) {
    for (ObservableLongMeasurement observable : observables) {
      add(observable);
    }
    return this;
  }

  private void add(Object object) {
    if (!(object instanceof SdkObservableMeasurement)) {
      logger.log(
          Level.WARNING,
          "BatchCallbackBuilder#add called with instruments that were not created by the SDK");
      return;
    }
    SdkObservableMeasurement observableMeasurement = (SdkObservableMeasurement) object;
    if (!meterSharedState
        .getInstrumentationScopeInfo()
        .equals(observableMeasurement.getInstrumentationScopeInfo())) {
      logger.log(
          Level.WARNING,
          "BatchCallbackBuilder#add called with instruments that belong to a different Meter");
      return;
    }
    observableMeasurements.add(observableMeasurement);
  }

  @Override
  public BatchCallback build(Runnable runnable) {
    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(observableMeasurements, runnable);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }
}
