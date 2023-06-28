/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {

  private static final Logger logger = Logger.getLogger(SdkMeter.class.getName());

  /**
   * Instrument names MUST conform to the following syntax.
   *
   * <ul>
   *   <li>They are not null or empty strings.
   *   <li>They are case-insensitive, ASCII strings.
   *   <li>The first character must be an alphabetic character.
   *   <li>Subsequent characters must belong to the alphanumeric characters, '_', '.', and '-'.
   *   <li>They can have a maximum length of 63 characters.
   * </ul>
   */
  private static final Pattern VALID_INSTRUMENT_NAME_PATTERN =
      Pattern.compile("([A-Za-z]){1}([A-Za-z0-9\\_\\-\\.]){0,62}");

  private static final Meter NOOP_METER = MeterProvider.noop().get("noop");
  private static final String NOOP_INSTRUMENT_NAME = "noop";

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      List<RegisteredReader> registeredReaders) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationScopeInfo, registeredReaders);
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /** Collect all metrics for the meter. */
  Collection<MetricData> collectAll(RegisteredReader registeredReader, long epochNanos) {
    return meterSharedState.collectAll(registeredReader, meterProviderSharedState, epochNanos);
  }

  /** Reset the meter, clearing all registered instruments. */
  void resetForTest() {
    this.meterSharedState.resetForTest();
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return !checkValidInstrumentName(name)
        ? NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkLongCounter.SdkLongCounterBuilder(
            meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return !checkValidInstrumentName(name)
        ? NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkLongUpDownCounter.SdkLongUpDownCounterBuilder(
            meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return !checkValidInstrumentName(name)
        ? NOOP_METER.histogramBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkDoubleHistogram.SdkDoubleHistogramBuilder(
            meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return !checkValidInstrumentName(name)
        ? NOOP_METER.gaugeBuilder(NOOP_INSTRUMENT_NAME)
        : new SdkDoubleGaugeBuilder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... additionalMeasurements) {
    Set<ObservableMeasurement> measurements = new HashSet<>();
    measurements.add(observableMeasurement);
    Collections.addAll(measurements, additionalMeasurements);

    List<SdkObservableMeasurement> sdkMeasurements = new ArrayList<>();
    for (ObservableMeasurement measurement : measurements) {
      if (!(measurement instanceof SdkObservableMeasurement)) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that were not created by the SDK.");
        continue;
      }
      SdkObservableMeasurement sdkMeasurement = (SdkObservableMeasurement) measurement;
      if (!meterSharedState
          .getInstrumentationScopeInfo()
          .equals(sdkMeasurement.getInstrumentationScopeInfo())) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that belong to a different Meter.");
        continue;
      }
      sdkMeasurements.add(sdkMeasurement);
    }

    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(sdkMeasurements, callback);
    meterSharedState.registerCallback(callbackRegistration);
    return new SdkObservableInstrument(meterSharedState, callbackRegistration);
  }

  @Override
  public String toString() {
    return "SdkMeter{instrumentationScopeInfo=" + instrumentationScopeInfo + "}";
  }

  /** Check if the instrument name is valid. If invalid, log a warning. */
  // Visible for testing
  static boolean checkValidInstrumentName(String name) {
    if (name != null && VALID_INSTRUMENT_NAME_PATTERN.matcher(name).matches()) {
      return true;
    }
    if (logger.isLoggable(Level.WARNING)) {
      logger.log(
          Level.WARNING,
          "Instrument name \""
              + name
              + "\" is invalid, returning noop instrument. Instrument names must consist of 63 or fewer characters including alphanumeric, _, ., -, and start with a letter.",
          new AssertionError());
    }

    return false;
  }
}
