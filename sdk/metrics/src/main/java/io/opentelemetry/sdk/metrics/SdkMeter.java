/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {

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
  private static final String VALID_INSTRUMENT_PATTERN =
      "([A-Za-z]){1}([A-Za-z0-9\\_\\-\\.]){0,62}";

  private static final Pattern VALID_INSTRUMENT_NAME = Pattern.compile(VALID_INSTRUMENT_PATTERN);

  private static final Logger logger = Logger.getLogger(SdkMeter.class.getName());

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationScopeInfo);
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /** Collects all the metric recordings that changed since the previous call. */
  Collection<MetricData> collectAll(
      CollectionInfo collectionInfo, long epochNanos, boolean suppressSynchronousCollection) {
    return meterSharedState.collectAll(
        collectionInfo, meterProviderSharedState, epochNanos, suppressSynchronousCollection);
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return !isValidName(name)
        ? MeterProvider.noop().meterBuilder(name).build().counterBuilder(name)
        : new SdkLongCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return !isValidName(name)
        ? MeterProvider.noop().meterBuilder(name).build().upDownCounterBuilder(name)
        : new SdkLongUpDownCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return !isValidName(name)
        ? MeterProvider.noop().meterBuilder(name).build().histogramBuilder(name)
        : new SdkDoubleHistogram.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return !isValidName(name)
        ? MeterProvider.noop().meterBuilder(name).build().gaugeBuilder(name)
        : new SdkDoubleGaugeBuilder(meterProviderSharedState, meterSharedState, name);
  }

  // Visible for testing
  static boolean isValidName(String name) {
    if (name == null || !VALID_INSTRUMENT_NAME.matcher(name).matches()) {
      logger.log(
          Level.WARNING,
          "Instrument names \""
              + name
              + "\" is invalid, returning noop instrument. Valid instrument names must match "
              + VALID_INSTRUMENT_PATTERN
              + ".");
      return false;
    }
    return true;
  }
}
